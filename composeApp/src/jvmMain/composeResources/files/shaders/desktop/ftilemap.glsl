#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2D uMaskTexture;// temporary unused
uniform sampler2D uTileTexture;

uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec2 uTextureScale;// map_size_in_tiles / texture_size_in_tiles
uniform vec4 uColorTint;


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

void main() {

    bool isSet = texture(uTileMap, vTexCord).r > 0u;
    vec2 tileMapCoordinates = vTexCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile
    vec4 color = sampleTexture();

    if (isSet) {
        vec4 originalColor = texture(uTileTexture, vTexCord * uTextureScale) * uColorTint;
        FragColor = color;
    } else {
        int mask = 0;

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


        if (hasSet(offTopTexCord)) mask |= 3;// Top
        if (hasSet(offRightTexCord)) mask |= 5;// Right
        if (hasSet(offBottomTexCord)) mask |= 12;// Bottom
        if (hasSet(offLeftTexCord)) mask |= 10;// Left

        if (hasSet(offTopRightTexCord)) mask |= 1;// Top-Right
        if (hasSet(offTopLeftTexCord)) mask |= 2;// Top-Left
        if (hasSet(offBottomRightTexCord)) mask |= 4;// Bottom-Right
        if (hasSet(offBottomLeftTexCord)) mask |= 8;// Bottom-Left

        if (mask == 0) {
            discard;
        } else {
            bool shouldColor = (withinBounds(tileCoordinates, vec2(0.5, 0.0), vec2(1, 0.5)) && (mask & 1) != 0) ||
                (withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(0.5, 0.5)) && (mask & 2) != 0) ||
                (withinBounds(tileCoordinates, vec2(0.5, 0.5), vec2(1, 1)) && (mask & 4) != 0) ||
                (withinBounds(tileCoordinates, vec2(0.0, 0.5), vec2(0.5, 1)) && (mask & 8) != 0);

            if (shouldColor) FragColor = color;
            else discard;


        }
    }
}
