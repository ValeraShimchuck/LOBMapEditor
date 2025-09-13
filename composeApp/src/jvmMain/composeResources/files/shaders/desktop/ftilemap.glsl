#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uMaskTexture;
uniform sampler2DArray uOverlayTexture;
uniform sampler2D uTileTexture;

uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec2 uTextureScale;// map_size_in_tiles / texture_size_in_tiles
uniform vec4 uColorTint;
uniform vec2 uResolution;

// textureSize
// textureScale


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

vec4 blendPixels(vec4 src, vec4 dst) {
    return (src * src.a) + (dst * (1.0 - src.a));
}

vec4 getPixel(vec2 texCord) {
    // 0 - nothing
    // 1 - current tile to be drawn
    // 2 - draw0 overlay
    uint tileValue = texture(uTileMap, texCord).r;
    bool isSet = tileValue == 1u;
    bool drawOverlay = tileValue == 2u;
    vec2 tileMapCoordinates = texCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile
    vec2 maskCoordinates = vec2(tileCoordinates.x, 1.0 - tileCoordinates.y);

    vec2 offTop = vec2(0.0, -uTileUnit.y);
    vec2 offBottom = vec2(0.0, uTileUnit.y);
    vec2 offRight = vec2(uTileUnit.x, 0.0);
    vec2 offLeft = vec2(-uTileUnit.x, 0.0);

    vec2 offTopTexCord = texCord + offTop;
    vec2 offRightTexCord = texCord + offRight;
    vec2 offBottomTexCord = texCord + offBottom;
    vec2 offLeftTexCord = texCord + offLeft;

    vec2 offTopRightTexCord = texCord + offTop + offRight;
    vec2 offTopLeftTexCord = texCord + offTop + offLeft;
    vec2 offBottomRightTexCord = texCord + offBottom + offRight;
    vec2 offBottomLeftTexCord = texCord + offBottom + offLeft;

    int mask = checkSet(offTopTexCord, 0) | // Top
    checkSet(offRightTexCord, 1) | // Right
    checkSet(offBottomTexCord, 2) | // Bottom
    checkSet(offLeftTexCord, 3) | // Left
    checkSet(offTopRightTexCord, 4) | // Top-Right
    checkSet(offTopLeftTexCord, 5) | // Top-Left
    checkSet(offBottomRightTexCord, 6) | // Bottom-Right
    checkSet(offBottomLeftTexCord, 7); // Bottom-Left

    int cornerMask = (1 << 4) | (1 << 5) | (1 << 6) | (1 << 7);
    int neighborMask = mask & 15;

    vec4 defaultOverlay = texture(uOverlayTexture, vec3(tileCoordinates.x, tileCoordinates.y, 0.0));
    vec4 overlayValue = texture(uOverlayTexture, vec3(tileCoordinates.x, tileCoordinates.y, float(neighborMask)));
    float defaultMask = texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, 0.0)).a;
    float maskValue = texture(uMaskTexture, vec3(tileCoordinates.x, tileCoordinates.y, float(neighborMask))).a;

    // 0 1 = 3
    // 1 2 = 6
    // 1 3 = 9
    // 2 3 = 12

    int leftBottomBitMask = 3 | (1 << 7);
    int leftTopBitMask = 6 | (1 << 5);
    int rightBottomBitMask = 9 | (1 << 6);
    int rightTopBitMask = 12 | (1 << 4);

    bool anyDoubleMaskPattern = any(equal(
        ivec4(mask & leftBottomBitMask, mask & leftTopBitMask, mask & rightBottomBitMask, mask & rightTopBitMask),
        ivec4(leftBottomBitMask, leftTopBitMask, rightBottomBitMask, rightTopBitMask)
    ));

    bool isProblematicSection = neighborMask > 0 && neighborMask % 3 == 0;
//    bool anyBlockedMask = (mask & 15) == 15 || (mask & 14) == 14 || (mask & 13) == 13 || (mask & 12) == 12
//    || (mask & 11) == 11 || (mask & 14) == 14;


    float higherMedium = 0.5;
    float lowerMedium = 1.0 - higherMedium;

    bool isTopRight = withinBounds(tileCoordinates, vec2(lowerMedium, 0.0), vec2(1, higherMedium));
    bool isTopLeft = withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(higherMedium, higherMedium));
    bool isBottomRight = withinBounds(tileCoordinates, vec2(lowerMedium, lowerMedium), vec2(1, 1));
    bool isBottomLeft = withinBounds(tileCoordinates, vec2(0.0, lowerMedium), vec2(higherMedium, 1));

    int currentCorner = (int(isTopLeft) << 0) | (int(isTopRight) << 1)
    | (int(isBottomLeft) << 2) | (int(isBottomRight) << 3);

    // 0 - top left corner | 1
    // 1 - top right corner | 2
    // 2 - bottom left corner | 4
    // 3 - bottom right corner | 8
    int[16] allowedDefaultOverlaysCorners = int[16](
        15,
        12,
        5,
        4,
        3,
        0,
        1,
        0,
        10,
        8,
        0,
        0,
        2,
        0,
        0,
        0
    );
    if ((mask & cornerMask) != 0
    && (!isProblematicSection || anyDoubleMaskPattern)
    && (allowedDefaultOverlaysCorners[mask & 15] & currentCorner) != 0) {
        maskValue = max(defaultMask, maskValue);
        overlayValue = blendPixels(defaultOverlay, overlayValue);
    }

    if (isProblematicSection) higherMedium = 0.7;

    lowerMedium = 1.0 - higherMedium;

    isTopRight = withinBounds(tileCoordinates, vec2(lowerMedium, 0.0), vec2(1, higherMedium));
    isTopLeft = withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(higherMedium, higherMedium));
    isBottomRight = withinBounds(tileCoordinates, vec2(lowerMedium, lowerMedium), vec2(1, 1));
    isBottomLeft = withinBounds(tileCoordinates, vec2(0.0, lowerMedium), vec2(higherMedium, 1));



    bool shouldColorTopRight =  isTopRight && (mask & 19) != 0; // Top-Right bits: 0, 1, 4 = 19
    bool shouldColorTopLeft = isTopLeft && (mask & 41) != 0; // Top-Left bits: 0, 3, 5 = 41
    bool shouldColorBottomRight = isBottomRight && (mask & 70) != 0; // Bottom-Right bits: 2, 1, 6 = 70
    bool shouldColorBottomLeft = isBottomLeft && (mask & 140) != 0; // Bottom-Left bits: 2, 3, 7 = 140


    bool shouldColor = any(bvec4(
    shouldColorTopRight,
    shouldColorTopLeft,
    shouldColorBottomRight,
    shouldColorBottomLeft
    ));

    vec4 color = sampleTexture();
//    vec4 maskedColor = color * maskValue;
    vec4 maskedColor = vec4(color.rgb, maskValue);

    if (isSet) {
        return color;
//        return mix(color, vec4(0.5, 0.5, 1.0, 1.0), 0.3);
    } else {
        if (mask == 0 || !shouldColor) {
            return vec4(-1);
        } else {
            if (drawOverlay) return blendPixels(overlayValue, maskedColor);
            return maskedColor;
        }
    }
}


void main() {
//    FragColor = gaussianBlur3x3(vTexCord);
    FragColor = getPixel(vTexCord);
    if (any(lessThan(FragColor, vec4(0.0)))) discard;
    if (FragColor.a < 0.2) discard;
}