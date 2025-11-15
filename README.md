# PPS-24-whodunnit

[![Coverage Status](https://coveralls.io/repos/github/LuciaCastellucci/PPS-24-whodunnit/badge.svg?branch=dev)](https://coveralls.io/github/LuciaCastellucci/PPS-24-whodunnit?branch=dev)
[![Documentation](https://img.shields.io/badge/docs-online-blue)](https://luciacastellucci.github.io/PPS-24-whodunnit/)

## About
**Whodunnit** is an investigative game that places the player in the role of a detective tasked with solving automatically generated mystery cases. 
The objective is to identify the culprit of a crime by analyzing clues, interrogations, documents, and relationships among the characters involved in the case.

## Setup

### Prerequisites

- Java 22 or higher
- A valid [Groq API](https://console.groq.com/) key

### Installation

1. **Download the latest release**

   Download the JAR file from the [Releases](https://github.com/LuciaCastellucci/PPS-24-whodunnit/releases) section.

2. **Configure environment variables**

   Before running the application, you must set the required environment variable for the Groq LLM integration:

   **Linux/macOS:**
   ```bash
   export GROQ_API_KEY="your-api-key-here"
   ```

   **Windows (Command Prompt):**
   ```cmd
   set GROQ_API_KEY=your-api-key-here
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:GROQ_API_KEY="your-api-key-here"
   ```
 > **Note**: If the `GROQ_API_KEY` environment variable is not set, the game will still start but it will always use the same case regardless of the game's parameterization.
   
For convenience on Linux/macOS, you can create a setup script based on [setup.example.sh](setup.example.sh):

   ```bash
   cp setup.example.sh setup.sh
   # Edit setup.sh with your API key
   source setup.sh
   ```

3. **Run the application**

   ```bash
   java -jar PPS-24-whodunnit.jar
   ```

## How to Play
1. **Review the Evidence**: Carefully read the case file, suspect statements, and all available clues.
2. **Construct Your Graph**: Add entities (people, places, objects) and connect them with semantic relationships that reflect your hypotheses.
3. **Make Your Accusation**: Submit your final answer by accusing a suspect.

## Development
This project is built with Scala 3 and uses:
- **ScalaTest** for comprehensive unit testing
- **upickle** for JSON serialization/deserialization
- **Groq API** with GPT OSS 120B for LLM integration
- **ScalaFX** for the graphical user interface

## Authors
- [Lucia Castellucci](https://github.com/LuciaCastellucci)
- [Roberto Mitugno](https://github.com/robertomitugno)
- [Luca Samorè](https://github.com/LucaSamore)