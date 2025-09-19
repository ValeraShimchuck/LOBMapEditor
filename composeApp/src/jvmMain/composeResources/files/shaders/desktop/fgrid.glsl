#version 330 core
in vec2 vPosition;

uniform vec2 uOffset;
uniform vec2 uGridSize;
uniform float uGridThickness;
uniform vec4 uGridColor;

out vec4 FragColor;

void main() {

    vec2 gridPosition = mod(vPosition + uOffset, uGridSize);
    if (!any(greaterThan(gridPosition, uGridSize - uGridThickness))) discard;
    FragColor = uGridColor;

}