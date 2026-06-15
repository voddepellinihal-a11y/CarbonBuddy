import re
import logging

logger = logging.getLogger(__name__)


def parse_utility_bill(ocr_text: str) -> dict:
    if not ocr_text or not ocr_text.strip():
        return {"error": "No text provided for OCR parsing"}

    total_kwh = _extract_kwh(ocr_text)
    billing_start = _extract_date(ocr_text, "start")
    billing_end = _extract_date(ocr_text, "end")
    utility_type = _detect_utility_type(ocr_text)

    return {
        "total_kwh": total_kwh,
        "billing_start": billing_start,
        "billing_end": billing_end,
        "utility_type": utility_type,
        "raw_text_snippet": ocr_text[:500],
        "confidence": "high" if total_kwh else "low",
    }


def _extract_kwh(text: str) -> float | None:
    patterns = [
        r"(?:total\s+)?(?:energy|consumption|usage|kWh)\s*:?\s*([\d,]+\.?\d*)",
        r"(?:unit|units)\s*(?:consumed|used)?\s*:?\s*([\d,]+\.?\d*)",
        r"(\d[\d,]*\.?\d*)\s*(?:kWh|KWH|kwh)",
    ]
    for pattern in patterns:
        match = re.search(pattern, text, re.IGNORECASE)
        if match:
            try:
                return float(match.group(1).replace(",", ""))
            except ValueError:
                continue
    return None


def _extract_date(text: str, which: str) -> str | None:
    date_pattern = r"(\d{1,2}[/-]\d{1,2}[/-]\d{2,4})"
    dates = re.findall(date_pattern, text)
    if len(dates) >= 2:
        return dates[0] if which == "start" else dates[1]
    elif dates:
        return dates[0]
    return None


def _detect_utility_type(text: str) -> str:
    text_lower = text.lower()
    if "electric" in text_lower or "electricity" in text_lower or "energy bill" in text_lower:
        return "electricity"
    if "gas" in text_lower:
        return "gas"
    if "water" in text_lower:
        return "water"
    return "electricity"
