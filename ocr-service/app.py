import logging
from flask import Flask, request, jsonify
from ocr_processor import parse_utility_bill

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "healthy"})


@app.route("/parse-bill", methods=["POST"])
def parse_bill():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body is required"}), 400

    ocr_text = data.get("ocr_text", "")
    result = parse_utility_bill(ocr_text)
    return jsonify(result)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
