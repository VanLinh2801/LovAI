.PHONY: help install run test clean

help:
	@echo "ImgGen - Couple Image Generator v2.0"
	@echo ""
	@echo "Commands:"
	@echo "  make install  - Setup environment"
	@echo "  make run      - Run server"
	@echo "  make test     - Test API"
	@echo "  make docker   - Run with Docker"
	@echo "  make clean    - Clean up"

install:
	uv venv
	source .venv/bin/activate && uv pip install -r requirements.txt

run:
	@echo "Starting server..."
	@source .venv/bin/activate && python main.py

test:
	@./test_curl.sh

docker:
	docker compose up -d
	@echo "Server running at http://localhost:6001"

clean:
	rm -rf .venv output/*.png output/*.json
	docker compose down

logs:
	docker compose logs -f

health:
	@curl -s http://localhost:6001/api/health | jq

styles:
	@curl -s http://localhost:6001/api/styles | jq
