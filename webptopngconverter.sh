#!/bin/bash

# WebP to PNG converter
# Usage: ./webp_to_png.sh input.webp [output.png]

# Check if input file is provided
if [ $# -eq 0 ]; then
    echo "Error: No input file specified."
    echo "Usage: $0 input.webp [output.png]"
    exit 1
fi

INPUT_FILE="$1"

# Check if input file exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file '$INPUT_FILE' not found."
    exit 1
fi

# Check if input file is a WebP
if [[ ! "$INPUT_FILE" =~ \.webp$ ]]; then
    echo "Error: Input file must have .webp extension."
    exit 1
fi

# Determine output file name
if [ $# -eq 2 ]; then
    OUTPUT_FILE="$2"
else
    # Get the directory and base name of input file
    INPUT_DIR=$(dirname "$INPUT_FILE")
    INPUT_BASE=$(basename "$INPUT_FILE" .webp)
    OUTPUT_FILE="$INPUT_DIR/${INPUT_BASE}.png"
fi

# Check for required tool
if ! command -v dwebp &> /dev/null; then
    echo "Error: 'dwebp' command not found. Please install it."
    echo "On Ubuntu/Debian: sudo apt-get install webp"
    echo "On Fedora/RHEL: sudo dnf install libwebp-tools"
    echo "On Arch: sudo pacman -S libwebp"
    exit 1
fi

# Convert WebP to PNG
echo "Converting WebP to PNG..."
echo "Input:  $INPUT_FILE"
echo "Output: $OUTPUT_FILE"

dwebp "$INPUT_FILE" -o "$OUTPUT_FILE"

if [ $? -ne 0 ] || [ ! -f "$OUTPUT_FILE" ]; then
    echo "Error: Failed to convert WebP to PNG."
    exit 1
fi

echo "Conversion completed successfully!"