#version 330 core

// Input from vertex shader
in vec4 vColor;
in vec2 vUV;
in float vTextureId;

// Output to framebuffer
out vec4 finalColor;

// Texture uniforms
#define MAX_TEXTURES 16
uniform sampler2D uTextures[MAX_TEXTURES];

void main() {
    // Select texture based on ID
    int texID = int(vTextureId + 0.5);

    // Clamp to valid range
    texID = clamp(texID, 0, MAX_TEXTURES - 1);

    // Sample texture (safe even if unbound)
    vec4 texColor = texture(uTextures[texID], vUV);

    finalColor = texColor * vColor;
}