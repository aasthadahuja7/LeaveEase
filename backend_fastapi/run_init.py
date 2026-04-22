#!/usr/bin/env python3
"""
Entry point script for running data initialization in Docker
"""

import sys
import os

# Add the app directory to Python path
sys.path.insert(0, '/app')

# Run the initialization
from app.init_data import init_database

if __name__ == "__main__":
    init_database()
