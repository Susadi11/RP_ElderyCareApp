"""
NLP Service for Reminder Text Processing using BERT
Handles natural language understanding for reminder creation
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict
import dateparser
from datetime import datetime
import re

app = FastAPI(title="NLP Service - BERT Reminder Parser")

# Try to import BERT dependencies
try:
    from transformers import pipeline
    BERT_AVAILABLE = True
    print("✅ BERT loaded successfully")
    
    # Initialize BERT models
    ner_pipeline = pipeline(
        "ner",
        model="dslim/bert-base-NER",
        aggregation_strategy="simple"
    )
except ImportError:
    BERT_AVAILABLE = False
    print("⚠️ BERT not available, using regex fallback")
    ner_pipeline = None


class ReminderParseRequest(BaseModel):
    text: str
    user_id: str
    

class ReminderParseResponse(BaseModel):
    title: str
    description: str
    category: str
    scheduled_time: str  # ISO format
    priority: str
    medication_names: List[str]
    entities: List[Dict]
    parser_used: str  # "bert" or "regex"


@app.get("/")
async def root():
    return {
        "service": "NLP Reminder Parser",
        "bert_available": BERT_AVAILABLE,
        "version": "1.0.0"
    }


@app.get("/health")
async def health():
    return {
        "status": "healthy",
        "bert_available": BERT_AVAILABLE
    }


@app.post("/parse-reminder", response_model=ReminderParseResponse)
async def parse_reminder(request: ReminderParseRequest):
    """Parse natural language text into structured reminder data"""
    
    if BERT_AVAILABLE:
        return parse_with_bert(request.text, request.user_id)
    else:
        return parse_with_regex(request.text, request.user_id)


def parse_with_bert(text: str, user_id: str) -> ReminderParseResponse:
    """Use BERT for advanced NLP parsing"""
    
    # Extract named entities
    entities = ner_pipeline(text)
    
    # Extract medication names
    medication_names = []
    for entity in entities:
        if entity['entity_group'] in ['MISC', 'ORG']:
            word = entity['word'].strip()
            if is_likely_medication(word):
                medication_names.append(word)
    
    # Determine category
    category = determine_category_bert(text, entities)
    
    # Parse date/time
    scheduled_time = parse_datetime_advanced(text)
    
    # Generate title
    title = generate_title(text, category, medication_names)
    
    # Determine priority
    priority = determine_priority(text)
    
    return ReminderParseResponse(
        title=title,
        description=text,
        category=category,
        scheduled_time=scheduled_time.isoformat() if scheduled_time else datetime.now().isoformat(),
        priority=priority,
        medication_names=medication_names,
        entities=[
            {"word": e['word'], "entity_group": e['entity_group'], "score": e['score']}
            for e in entities
        ],
        parser_used="bert"
    )


def parse_with_regex(text: str, user_id: str) -> ReminderParseResponse:
    """Fallback regex-based parsing"""
    
    text_lower = text.lower()
    
    # Category detection
    if any(word in text_lower for word in ['medicine', 'medication', 'pill', 'tablet', 'dose']):
        category = 'medication'
    elif any(word in text_lower for word in ['appointment', 'doctor', 'clinic', 'hospital']):
        category = 'appointment'
    elif any(word in text_lower for word in ['meal', 'breakfast', 'lunch', 'dinner', 'eat']):
        category = 'meal'
    elif any(word in text_lower for word in ['exercise', 'walk', 'gym', 'workout']):
        category = 'exercise'
    else:
        category = 'other'
    
    # Parse date/time
    scheduled_time = parse_datetime_advanced(text)
    
    # Title generation
    title = generate_title(text, category, [])
    
    # Priority
    priority = determine_priority(text)
    
    return ReminderParseResponse(
        title=title,
        description=text,
        category=category,
        scheduled_time=scheduled_time.isoformat() if scheduled_time else datetime.now().isoformat(),
        priority=priority,
        medication_names=[],
        entities=[],
        parser_used="regex"
    )


def is_likely_medication(word: str) -> bool:
    """Check if word is likely a medication name"""
    medication_suffixes = ['in', 'ol', 'cin', 'mycin', 'statin', 'pril', 'ide']
    word_lower = word.lower()
    
    # Check common suffixes
    if any(word_lower.endswith(suffix) for suffix in medication_suffixes):
        return True
    
    # Check if it's a known medication
    common_meds = ['aspirin', 'insulin', 'metformin', 'lisinopril', 'atorvastatin']
    if word_lower in common_meds:
        return True
    
    return False


def determine_category_bert(text: str, entities: List) -> str:
    """Use BERT entities to determine category"""
    text_lower = text.lower()
    
    # Check entities first
    for entity in entities:
        word = entity['word'].lower()
        if is_likely_medication(word):
            return 'medication'
    
    # Fallback to keyword matching
    if any(word in text_lower for word in ['medicine', 'medication', 'pill', 'take', 'dose']):
        return 'medication'
    elif any(word in text_lower for word in ['appointment', 'doctor', 'clinic']):
        return 'appointment'
    elif any(word in text_lower for word in ['meal', 'eat', 'breakfast', 'lunch', 'dinner']):
        return 'meal'
    elif any(word in text_lower for word in ['exercise', 'walk', 'workout']):
        return 'exercise'
    
    return 'other'


def parse_datetime_advanced(text: str) -> Optional[datetime]:
    """Advanced date/time parsing using dateparser"""
    
    # Use dateparser for natural language
    parsed_date = dateparser.parse(
        text,
        settings={
            'PREFER_DATES_FROM': 'future',
            'RETURN_AS_TIMEZONE_AWARE': False
        }
    )
    
    return parsed_date


def generate_title(text: str, category: str, medication_names: List[str]) -> str:
    """Generate a concise title from the text"""
    
    if medication_names:
        return f"Take {medication_names[0]}"
    
    if category == 'medication':
        return "Take Medication"
    elif category == 'appointment':
        return "Doctor Appointment"
    elif category == 'meal':
        return "Meal Reminder"
    elif category == 'exercise':
        return "Exercise Time"
    
    # Extract first few words
    words = text.split()[:4]
    return " ".join(words).capitalize()


def determine_priority(text: str) -> str:
    """Determine priority based on urgency keywords"""
    text_lower = text.lower()
    
    if any(word in text_lower for word in ['urgent', 'critical', 'emergency', 'immediately']):
        return 'critical'
    elif any(word in text_lower for word in ['important', 'must', 'essential']):
        return 'high'
    elif any(word in text_lower for word in ['optional', 'if possible', 'maybe']):
        return 'low'
    
    return 'medium'


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
