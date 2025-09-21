#version 330 core
in vec2 vUVMapping;

uniform sampler2D uTexture;
uniform vec4 uColorTint;

out vec4 FragColor;

void main() {
    FragColor = texture(uTexture, vUVMapping) * uColorTint;
//    FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}