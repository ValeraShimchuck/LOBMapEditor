#version 330 core  // Targeting widely supported OpenGL 3.3

#define SHADER_NAME batch-vertex

// Vertex attributes
layout(location = 0) in vec2 aPosition;
layout(location = 1) in vec2 aUV;
layout(location = 2) in vec4 aColor;
layout(location = 3) in vec2 aTextureIdAndRound;

// Output to fragment shader
out vec4 vColor;
out vec2 vUV;
out float vTextureId;

// Uniforms
uniform mat3 uProjectionMatrix;
uniform mat3 uWorldTransformMatrix;
uniform vec4 uWorldColorAlpha;
uniform vec2 uResolution;

vec2 roundPixels(vec2 position, vec2 targetSize) {
    return (floor(((position * 0.5 + 0.5) * targetSize) + 0.5) / targetSize) * 2.0 - 1.0;
}

void main() {
    // Simplified identity matrix initialization
    mat3 modelMatrix = mat3(1.0);

    // Color processing
    vColor = vec4(aColor.rgb * aColor.a, aColor.a);
    vTextureId = aTextureIdAndRound.y;
    vUV = aUV;

    // Position transformation
    vec3 worldPos = uProjectionMatrix * uWorldTransformMatrix * modelMatrix * vec3(aPosition, 1.0);
    gl_Position = vec4(worldPos.xy, 0.0, 1.0);
    vColor *= uWorldColorAlpha;

    // Pixel rounding
    if (aTextureIdAndRound.x > 0.5) {  // Optimized float comparison
        gl_Position.xy = roundPixels(gl_Position.xy, uResolution);
    }
}