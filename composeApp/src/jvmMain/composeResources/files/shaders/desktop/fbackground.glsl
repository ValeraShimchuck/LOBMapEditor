#version 330 core
in vec2 vTexCoord;
out vec4 FragColor;

uniform sampler2D uTileTexture;
uniform vec3 uTintColor = vec3(1.0); // Default no tint

void main() {
    // Use repeating texture wrap mode
    vec4 texColor = texture(uTileTexture, vTexCoord);

    // Apply optional color tint
    //FragColor = vec4(texColor.rgb  * uTintColor, texColor.a);
    FragColor = vec4(texColor.rgb, texColor.a);
}