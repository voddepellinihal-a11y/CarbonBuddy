import logging
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)

NUDGE_TEMPLATES = {
    "TRANSPORT": [
        {
            "title": "Switch to Metro for your daily commute",
            "description": "Taking the Hyderabad Metro instead of a personal vehicle saves ~3.5kg CO2 per round trip.",
            "savings_kg": 45.0,
        },
        {
            "title": "Try carpooling this week",
            "description": "Sharing rides with 3 colleagues cuts your commute emissions by 75%.",
            "savings_kg": 30.0,
        },
    ],
    "FOOD": [
        {
            "title": "Meatless Monday challenge",
            "description": "One plant-based day per week saves 500g CO2 per meal on average.",
            "savings_kg": 17.5,
        },
        {
            "title": "Order from local restaurants",
            "description": "Local food delivery has 30% lower delivery emissions than aggregator platforms.",
            "savings_kg": 8.0,
        },
    ],
    "UTILITY": [
        {
            "title": "Optimize your AC temperature",
            "description": "Setting AC to 24\u00b0C instead of 18\u00b0C can reduce electricity usage by 30%.",
            "savings_kg": 28.0,
        },
        {
            "title": "Unplug idle electronics",
            "description": "Standby power accounts for 10% of residential electricity use. Unplug when not in use.",
            "savings_kg": 12.0,
        },
    ],
}


def generate_nudges(top_category: str, carbon_records: list) -> list:
    templates = NUDGE_TEMPLATES.get(top_category, NUDGE_TEMPLATES["TRANSPORT"])
    nudges = []
    for i, t in enumerate(templates):
        nudges.append(
            {
                "id": f"nudge_{i}_{datetime.now().timestamp()}",
                "title": t["title"],
                "description": t["description"],
                "estimated_savings_kg": t["savings_kg"],
                "category": top_category,
                "priority": "high" if i == 0 else "medium",
            }
        )
    return nudges


def compute_weekly_summary(carbon_records: list) -> dict:
    total = sum(r.get("carbon_kg", 0) for r in carbon_records)
    by_category = {}
    for r in carbon_records:
        cat = r.get("category", "OTHER")
        by_category[cat] = by_category.get(cat, 0) + r.get("carbon_kg", 0)
    top_category = max(by_category, key=by_category.get) if by_category else "TRANSPORT"
    return {
        "total_carbon_kg": total,
        "by_category": by_category,
        "top_category": top_category,
        "record_count": len(carbon_records),
    }
