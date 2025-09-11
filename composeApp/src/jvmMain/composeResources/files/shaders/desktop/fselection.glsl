#version 330 core

in vec2 vPosition;
out vec4 FragColor;

uniform vec4 uColor;
uniform vec2 uSelectionMin;
uniform vec2 uSelectionMax;
uniform float uThickness;

bool withinBounds(vec2 toCheck, vec2 minBounds, vec2 maxBounds) {
    return toCheck.x >= minBounds.x && toCheck.x < maxBounds.x && toCheck.y >= minBounds.y && toCheck.y < maxBounds.y;
}

void main() {
//    FragColor = uColor;
    // Use the uniforms in a way that doesn't affect the output
    if (length(uSelectionMin) + length(uSelectionMax) + uThickness < -1.0) {
        FragColor = vec4(0.0, 1.0, 0.0, 1.0); // This will never happen, but prevents optimization
    } else FragColor = uColor;
//    if (withinBounds(vPosition, uSelectionMin + uThickness, uSelectionMax - uThickness)) discard;
}