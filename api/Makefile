# Detect OS
UNAME_S := $(OS)
ifeq ($(UNAME_S),Windows_NT)
	# Windows
    VENV := .venv\Scripts
    PYTHON := $(VENV)\python.exe
    PIP := $(PYTHON) -m pip
    RM := rmdir /s /q
    SHELL := cmd.exe
    .SHELLFLAGS := /C
else
	# Linux and MacOS
    UNAME_S := $(shell uname -s)
    VENV := .venv/bin
    PYTHON := $(VENV)/python3
    PIP := $(PYTHON) -m pip
    RM := rm -rf
endif

# Default target: run main.py
run: venv
	$(PYTHON) main.py

# Open a shell inside the virtual environment
shell: venv
ifeq ($(OS),Windows_NT)
	@cmd /K .venv\Scripts\activate.bat
else
	@bash --rcfile <(echo "source .venv/bin/activate")
endif

# Create virtual environment if missing and install deps
venv:
ifeq ($(OS),Windows_NT)
	if not exist .venv python -m venv .venv 
	if exist requirements.txt $(PIP) install -r requirements.txt
else
	test -d .venv || python3 -m venv .venv
	@if [ -f requirements.txt ]; then $(PIP) install -r requirements.txt; fi
endif

# Freeze current dependencies into requirements.txt
freeze:
	$(PIP) freeze > requirements.txt

# Remove the virtual environment
clean:
	$(RM) .venv

.PHONY: run venv clean freeze shell
