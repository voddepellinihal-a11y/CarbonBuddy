import logging
from flask import Flask, request, jsonify
from nudge_engine import generate_nudges, compute_weekly_summary

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy"})


@app.route("/nudges", methods=["POST"])
def nudges():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body is required"}), 400

    top_category = data.get("top_category", "TRANSPORT")
    records = data.get("records", [])
    result = generate_nudges(top_category, records)
    return jsonify({"nudges": result})


@app.route("/weekly-summary", methods=["POST"])
def weekly_summary():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body is required"}), 400

    records = data.get("records", [])
    result = compute_weekly_summary(records)
    return jsonify(result)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=False)
