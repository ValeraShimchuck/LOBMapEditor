#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord; // not used

uniform mat4 uInvViewProjection;

out vec2 vTexCoord;

void main() {

    gl_Position = vec4(aPos, 0.0, 1.0);

    vec4 worldPos = uInvViewProjection * vec4(aPos, 0.0, 1.0);

    float scale = 0.001;


    vTexCoord = worldPos.xy * scale;
}