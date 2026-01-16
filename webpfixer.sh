#!/bin/bash

# WebP to PNG and back converter with minimal losses
# Usage: ./webp_convert.sh input.webp [output.webp]

# Check if input file is provided
if [ $# -eq 0 ]; then
    echo "Error: No input file specified."
    echo "Usage: $0 input.webp [output.webp]"
    exit 1
fi

INPUT_FILE="$1"
BACKUP_FILE="${2:-${INPUT_FILE%.webp}_old.webp}"

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

# Create a temporary directory
TEMP_DIR=$(mktemp -d)
PNG_FILE="$TEMP_DIR/temp.png"

# Check for required tools
check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "Error: '$1' command not found. Please install it."
        echo "On Ubuntu/Debian: sudo apt-get install $2"
        echo "On Fedora/RHEL: sudo dnf install $2"
        echo "On Arch: sudo pacman -S $2"
        exit 1
    fi
}

check_command "dwebp" "webp"
check_command "cwebp" "webp"
check_command "identify" "imagemagick"

# Get original WebP info
echo "Original WebP file: $INPUT_FILE"
ORIGINAL_SIZE=$(stat -c%s "$INPUT_FILE" 2>/dev/null || stat -f%z "$INPUT_FILE")
#echo "Original size: $(numfmt --to=iec --format="%.2f" $ORIGINAL_SIZE)B"

# Step 1: Convert WebP to PNG with minimal loss using dwebp
echo "Converting WebP to PNG..."
dwebp "$INPUT_FILE" -o "$PNG_FILE"

if [ $? -ne 0 ] || [ ! -f "$PNG_FILE" ]; then
    echo "Error: Failed to convert WebP to PNG."
    rm -rf "$TEMP_DIR"
    exit 1
fi

mv $INPUT_FILE $BACKUP_FILE

# Step 2: Convert PNG back to WebP with minimal compression
echo "Converting PNG back to WebP..."
cwebp -lossless -z 0 -quiet "$PNG_FILE" -o "$INPUT_FILE"

if [ $? -ne 0 ]; then
    echo "Error: Failed to convert PNG to WebP."
    rm -rf "$TEMP_DIR"
    exit 1
fi


# Get new WebP info
NEW_SIZE=$(stat -c%s "$INPUT_FILE" 2>/dev/null || stat -f%z "$INPUT_FILE")
echo "New WebP file: $INPUT_FILE"
echo "New size: $(numfmt --to=iec --format="%.2f" $NEW_SIZE)B"

# Compare sizes
SIZE_DIFF=$((NEW_SIZE - ORIGINAL_SIZE))
if [ $SIZE_DIFF -gt 0 ]; then
    echo "Size increase: +$(numfmt --to=iec --format="%.2f" $SIZE_DIFF)B"
elif [ $SIZE_DIFF -lt 0 ]; then
    echo "Size decrease: $(numfmt --to=iec --format="%.2f" $SIZE_DIFF)B"
else
    echo "Size unchanged"
fi

# Clean up temporary files
rm -rf "$TEMP_DIR"

echo "Conversion completed successfully!"