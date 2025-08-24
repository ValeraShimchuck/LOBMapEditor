#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uOverlayTexture;


uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec4 uColorTint;

// textureSize
// textureScale

float randf(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}


// max is exclusive
uint randi(vec2 co, uint max) {
    return uint(randf(co) * float(max));
}


vec4 getPixel(vec2 texCord) {
    // 0 - nothing
    // 1 - current tile to be drawn
    uint tileValue = texture(uTileMap, texCord).r;
    bool isSet = tileValue == 1u;
    vec2 tileMapCoordinates = texCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile

    int overlaySize = textureSize(uOverlayTexture, 0).z;

    vec4 overlayPixel = texture(uOverlayTexture, vec3(tileCoordinates, float(randi(vec2(ivec2(tileMapCoordinates)), uint(overlaySize)))));
    if (!isSet) return vec4(-1);
    return overlayPixel  * uColorTint;
}


void main() {
    //    FragColor = gaussianBlur3x3(vTexCord);
    FragColor = getPixel(vTexCord);
    if (any(lessThan(FragColor, vec4(0.0)))) discard;
//    if (FragColor.a < 0.2) discard;
}