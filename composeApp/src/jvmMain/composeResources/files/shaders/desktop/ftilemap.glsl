#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uMaskTexture;
uniform sampler2D uTileTexture;

uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec2 uTextureScale;// map_size_in_tiles / texture_size_in_tiles
uniform vec4 uColorTint;

const float kernel[3] = float[](0.27901, 0.44198, 0.27901); // Gaussian kernel



bool withinBounds(vec2 toCheck, vec2 minBounds, vec2 maxBounds) {
    return toCheck.x >= minBounds.x && toCheck.x < maxBounds.x && toCheck.y >= minBounds.y && toCheck.y < maxBounds.y;
}

bool withinUnit(vec2 cords) {
    return withinBounds(cords, vec2(0), vec2(1));
}

bool hasSet(vec2 offsetCords) {
    return withinUnit(offsetCords) && texture(uTileMap, offsetCords).r > 0u;
}

vec4 sampleTexture() {
    return texture(uTileTexture, vTexCord * uTextureScale) * uColorTint;
}

int checkSet(vec2 offsetCords, int bitToSet) {
    return (int(withinUnit(offsetCords)) & int(texture(uTileMap, offsetCords).r)) << bitToSet;
}

vec4 getPixel(texCord) {
    bool isSet = texture(uTileMap, vTexCord).r > 0u;
    vec2 tileMapCoordinates = vTexCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile
    vec2 maskCoordinates = vec2(tileCoordinates.x, 1.0 - tileCoordinates.y);

    vec2 offTop = vec2(0.0, -uTileUnit.y);
    vec2 offBottom = vec2(0.0, uTileUnit.y);
    vec2 offRight = vec2(uTileUnit.x, 0.0);
    vec2 offLeft = vec2(-uTileUnit.x, 0.0);

    vec2 offTopTexCord = vTexCord + offTop;
    vec2 offRightTexCord = vTexCord + offRight;
    vec2 offBottomTexCord = vTexCord + offBottom;
    vec2 offLeftTexCord = vTexCord + offLeft;

    vec2 offTopRightTexCord = vTexCord + offTop + offRight;
    vec2 offTopLeftTexCord = vTexCord + offTop + offLeft;
    vec2 offBottomRightTexCord = vTexCord + offBottom + offRight;
    vec2 offBottomLeftTexCord = vTexCord + offBottom + offLeft;

    int mask = checkSet(offTopTexCord, 0) | // Top
    checkSet(offRightTexCord, 1) | // Right
    checkSet(offBottomTexCord, 2) | // Bottom
    checkSet(offLeftTexCord, 3) | // Left
    checkSet(offTopRightTexCord, 4) | // Top-Right
    checkSet(offTopLeftTexCord, 5) | // Top-Left
    checkSet(offBottomRightTexCord, 6) | // Bottom-Right
    checkSet(offBottomLeftTexCord, 7); // Bottom-Left

    float defaultMask = texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, 0.0)).a;
    float maskValue = max(defaultMask,
    texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, float(mask & 15))).a
    );



    bool shouldColor = (withinBounds(tileCoordinates, vec2(0.5, 0.0), vec2(1, 0.5)) && (mask & 19) != 0) || // Top-Right bits: 0, 1, 4 = 19
    (withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(0.5, 0.5)) && (mask & 41) != 0) || // Top-Left bits: 0, 3, 5 = 41
    (withinBounds(tileCoordinates, vec2(0.5, 0.5), vec2(1, 1)) && (mask & 70) != 0) || // Bottom-Right bits: 2, 1, 6 = 70
    (withinBounds(tileCoordinates, vec2(0.0, 0.5), vec2(0.5, 1)) && (mask & 140) != 0); // Bottom-Left bits: 2, 3, 7 = 140

    vec4 color = sampleTexture();
    vec4 maskedColor = color * maskValue;

    if (isSet) {
        return color;
    } else {
        if (mask == 0 || !shouldColor) {
            return vec4(-1);
        } else {
            return maskedColor;
        }
    }
}

void main() {

    bool isSet = texture(uTileMap, vTexCord).r > 0u;
    vec2 tileMapCoordinates = vTexCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile
    vec2 maskCoordinates = vec2(tileCoordinates.x, 1.0 - tileCoordinates.y);

    vec2 offTop = vec2(0.0, -uTileUnit.y);
    vec2 offBottom = vec2(0.0, uTileUnit.y);
    vec2 offRight = vec2(uTileUnit.x, 0.0);
    vec2 offLeft = vec2(-uTileUnit.x, 0.0);

    vec2 offTopTexCord = vTexCord + offTop;
    vec2 offRightTexCord = vTexCord + offRight;
    vec2 offBottomTexCord = vTexCord + offBottom;
    vec2 offLeftTexCord = vTexCord + offLeft;

    vec2 offTopRightTexCord = vTexCord + offTop + offRight;
    vec2 offTopLeftTexCord = vTexCord + offTop + offLeft;
    vec2 offBottomRightTexCord = vTexCord + offBottom + offRight;
    vec2 offBottomLeftTexCord = vTexCord + offBottom + offLeft;

    int mask = checkSet(offTopTexCord, 0) | // Top
    checkSet(offRightTexCord, 1) | // Right
    checkSet(offBottomTexCord, 2) | // Bottom
    checkSet(offLeftTexCord, 3) | // Left
    checkSet(offTopRightTexCord, 4) | // Top-Right
    checkSet(offTopLeftTexCord, 5) | // Top-Left
    checkSet(offBottomRightTexCord, 6) | // Bottom-Right
    checkSet(offBottomLeftTexCord, 7); // Bottom-Left

    float defaultMask = texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, 0.0)).a;
    float maskValue = max(defaultMask,
        texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, float(mask & 15))).a
    );



    bool shouldColor = (withinBounds(tileCoordinates, vec2(0.5, 0.0), vec2(1, 0.5)) && (mask & 19) != 0) || // Top-Right bits: 0, 1, 4 = 19
    (withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(0.5, 0.5)) && (mask & 41) != 0) || // Top-Left bits: 0, 3, 5 = 41
    (withinBounds(tileCoordinates, vec2(0.5, 0.5), vec2(1, 1)) && (mask & 70) != 0) || // Bottom-Right bits: 2, 1, 6 = 70
    (withinBounds(tileCoordinates, vec2(0.0, 0.5), vec2(0.5, 1)) && (mask & 140) != 0); // Bottom-Left bits: 2, 3, 7 = 140

    vec4 color = sampleTexture();
    vec4 maskedColor = color * maskValue;

    if (isSet) {
        FragColor = color;
    } else {
        if (mask == 0 || !shouldColor) {
            discard;
        } else {
            FragColor = maskedColor;
        }
    }
}