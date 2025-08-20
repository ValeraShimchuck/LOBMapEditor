#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uMaskTexture;
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

vec4 getPixel(vec2 texCord) {
    bool isSet = texture(uTileMap, texCord).r > 0u;
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

    if ((mask & cornerMask) != 0 && (!isProblematicSection || anyDoubleMaskPattern)) {
        maskValue = max(defaultMask, maskValue);
    }


    float higherMedium = 0.5;
    if (isProblematicSection) higherMedium = 0.7;

    float lowerMedium = 1.0 - higherMedium;

    bool shouldColorTopRight = withinBounds(tileCoordinates, vec2(lowerMedium, 0.0), vec2(1, higherMedium)) && (mask & 19) != 0; // Top-Right bits: 0, 1, 4 = 19
    bool shouldColorTopLeft = (withinBounds(tileCoordinates, vec2(0.0, 0.0), vec2(higherMedium, higherMedium)) && (mask & 41) != 0); // Top-Left bits: 0, 3, 5 = 41
    bool shouldColorBottomRight = (withinBounds(tileCoordinates, vec2(lowerMedium, lowerMedium), vec2(1, 1)) && (mask & 70) != 0); // Bottom-Right bits: 2, 1, 6 = 70
    bool shouldColorBottomLeft = (withinBounds(tileCoordinates, vec2(0.0, lowerMedium), vec2(higherMedium, 1)) && (mask & 140) != 0); // Bottom-Left bits: 2, 3, 7 = 140

    bool shouldColor = any(bvec4(shouldColorTopRight, shouldColorTopLeft, shouldColorBottomRight, shouldColorBottomLeft));

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

//vec4 blur(vec2 texCoord) {
//    // Fixed blur parameters
//    float blurAmount = 2.0;
//    vec2 texSize = textureSize(uTileTexture, 0);
//    vec2 texOffset = vec2(1.0, 1.0) / texSize;
//
//    // Proper Gaussian weights for 5 samples (sum = ~1.0)
//    float weight[3] = float[3](0.441, 0.279, 0.079);
//
//    // Sample the center pixel
//    vec4 blurredColor = texture(uTileTexture, texCoord) * weight[0];
//
//    // Sample in four directions (horizontal and vertical)
//    blurredColor += texture(uTileTexture, texCoord + vec2(texOffset.x * blurAmount, 0.0)) * weight[1];
//    blurredColor += texture(uTileTexture, texCoord - vec2(texOffset.x * blurAmount, 0.0)) * weight[1];
//    blurredColor += texture(uTileTexture, texCoord + vec2(0.0, texOffset.y * blurAmount)) * weight[1];
//    blurredColor += texture(uTileTexture, texCoord - vec2(0.0, texOffset.y * blurAmount)) * weight[1];
//
//    // Sample diagonally with lower weight
//    blurredColor += texture(uTileTexture, texCoord + texOffset * blurAmount) * weight[2];
//    blurredColor += texture(uTileTexture, texCoord - texOffset * blurAmount) * weight[2];
//    blurredColor += texture(uTileTexture, texCoord + vec2(texOffset.x, -texOffset.y) * blurAmount) * weight[2];
//    blurredColor += texture(uTileTexture, texCoord + vec2(-texOffset.x, texOffset.y) * blurAmount) * weight[2];
//
//    return blurredColor * uColorTint;
//}

vec4 gaussianBlur3x3(vec2 uv) {

    vec4 color = vec4(0.0);

    // 3x3 Gaussian kernel weights
    float kernel[9] = float[](
    0.077847, 0.123317, 0.077847,
    0.123317, 0.195346, 0.123317,
    0.077847, 0.123317, 0.077847
    );
    //vec2 texSize = textureSize(uTileTexture, 0);
    vec2 texOffset = 1.0 / uResolution;

    int i = 0;
    for(int x = -1; x <= 1; x++) {
        for(int y = -1; y <= 1; y++) {
            vec2 offset = vec2(x, y) * texOffset;
            vec4 pixel = getPixel(uv + offset)  * kernel[i];
//            if (any(lessThan(pixel, vec4(0.0)))) {
//                if (x == 0 && y == 0) return vec4(-1);
//                continue;
//            }
            color += pixel;
            i++;
        }
    }

    return color;
}

void main() {
//    FragColor = gaussianBlur3x3(vTexCord);
    FragColor = getPixel(vTexCord);
    if (any(lessThan(FragColor, vec4(0.0)))) discard;
}