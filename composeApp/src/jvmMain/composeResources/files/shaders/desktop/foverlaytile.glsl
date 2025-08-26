#version 330 core

const int MAX_OVERLAYS = 16;
const int NEIGHBORS_SIZE = 9;
const int OVERLAY_NEIGHBORS_SIZE = MAX_OVERLAYS * NEIGHBORS_SIZE;
const ivec2 OVERLAY_NEIGHBORS_DIMENSIONS = ivec2(NEIGHBORS_SIZE, MAX_OVERLAYS);
in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uOverlayTexture;


uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec4 uColorTint;

uniform float uRandomRange;
uniform uint uOverlayAmount;
uniform float uScale;
uniform float uOffset;

// textureSize
// textureScale

bool withinBounds(vec2 toCheck, vec2 minBounds, vec2 maxBounds) {
    return toCheck.x >= minBounds.x && toCheck.x < maxBounds.x && toCheck.y >= minBounds.y && toCheck.y < maxBounds.y;
}

bool withinUnit(vec2 cords) {
    return withinBounds(cords, vec2(0), vec2(1));
}

float randf(vec2 co){
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}


// max is exclusive
uint randi(vec2 co, uint max) {
    return uint(randf(co) * float(max));
}

vec4 blendPixels(vec4 src, vec4 dst) {
    return (src * src.a) + (dst * (1.0 - src.a));
}

int getIndex2d(ivec2 pos, ivec2 dimensions) {
    return pos.y * dimensions.x + pos.x;
}

vec4 getPixel(vec2 texCord) {
    // 0 - nothing
    // 1 - current tile to be drawn
//    uint tileValue = texture(uTileMap, texCord).r;
//    bool isSet = tileValue == 1u;
    vec2 tileMapCoordinates = texCord / uTileUnit;
    vec2 tileCoordinates = fract(tileMapCoordinates);// [0..1] within a tile

    int overlayAtlasSize = textureSize(uOverlayTexture, 0).z;

//    if (withinBounds(tileCoordinates, vec2(0), vec2(0.02, 1.0))) return vec4(1.0, 0.0, 0.0, 1.0);
//    if (withinBounds(tileCoordinates, vec2(0), vec2(1.0, 0.02))) return vec4(1.0, 0.0, 0.0, 1.0);
//    if (withinBounds(tileCoordinates, vec2(0.8, 0), vec2(1.0, 1.0))) return vec4(1.0, 0.0, 0.0, 1.0);
//    if (withinBounds(tileCoordinates, vec2(0, 0.2), vec2(1.0, 1.0))) return vec4(1.0, 0.0, 0.0, 1.0);


    vec4[9] debugPixels;
    vec2[18] debugBoundries = vec2[18](

    vec2(0), vec2(0.33),
    vec2(0.33,0), vec2(0.66, 0.33),
    vec2(0.66,0), vec2(1, 0.33),

    vec2(0, 0.33), vec2(0.33, 0.66),
    vec2(0.33, 0.33), vec2(0.66, 0.66),
    vec2(0.66, 0.33), vec2(1, 0.66),


    vec2(0, 0.66), vec2(0.33, 1),
    vec2(0.33, 0.66), vec2(0.66, 1),
    vec2(0.66, 0.66), vec2(1, 1)
    );

    for (int i = 0; i < 9; i++) {
        debugPixels[i] = vec4(vec3(0.0), 1.0);
    }
//    debugPixels[0] = vec4(vec3(0.3), 1.0);
//    debugPixels[4] = vec4(1.0);
//    debugPixels[8] = vec4(vec3(0.7), 1.0);



    // 0 3 6
    // 1 4 7
    // 2 5 8

    ivec2[9] offsets;
    int currentOffsetPosition = 0;
    for (int x = -1; x < 2; x++) {
        for (int y = -1; y < 2; y++) {
            offsets[currentOffsetPosition++] = ivec2(x, y);
        }
    }



    bool[9] neighborTileStatus;


    for (int i = 0; i < 9; i++) {
        ivec2 offset = offsets[i];
        neighborTileStatus[i] = texture(uTileMap, texCord + vec2(offset) * uTileUnit).r == 1u;
//        if (neighborTileStatus[i]) debugPixels[i] = vec4(1.0);
    }



//    float[9][MAX_OVERLAYS] tileIndecies;
    float[OVERLAY_NEIGHBORS_SIZE] tileIndecies;


    for (int i = 0; i < 9; i++) {
        ivec2 offset = offsets[i];
        for (int currentOverlay = 0; currentOverlay < int(uOverlayAmount); currentOverlay++) {
            int index = getIndex2d(ivec2(i, currentOverlay), OVERLAY_NEIGHBORS_DIMENSIONS);
            tileIndecies[index] = float(randi(vec2((ivec2(tileMapCoordinates) + offset) * (currentOverlay + 1)), uint(overlayAtlasSize)));
        }
    }

//    vec2[9][MAX_OVERLAYS] randomOffsets;
    vec2[OVERLAY_NEIGHBORS_SIZE] randomOffsets;

    float doubleRandomRange = uRandomRange * 2;

    for (int i = 0; i < 9; i++) {
        ivec2 offset = offsets[i];
        for (int currentOverlay = 0; currentOverlay < int(uOverlayAmount); currentOverlay++) {
            int index = getIndex2d(ivec2(i, currentOverlay), OVERLAY_NEIGHBORS_DIMENSIONS);
            float xOffset = randf(vec2((ivec2(tileMapCoordinates) + offset) * (currentOverlay + 1) * 1)) * doubleRandomRange - uRandomRange;
            float yOffset = randf(vec2((ivec2(tileMapCoordinates) + offset) * (currentOverlay + 1) * 2)) * doubleRandomRange - uRandomRange;
           randomOffsets[index] = vec2(xOffset, yOffset);
//            if (currentOverlay == 0) debugPixels[i] =  vec4(randf(vec2((ivec2(tileMapCoordinates) + offset) * currentOverlay * 1)), randf(vec2((ivec2(tileMapCoordinates) + offset) * currentOverlay * 2)), 0.0, 1.0);
//            if (currentOverlay == 1) debugPixels[i] =  vec4(vec3(randf(vec2((ivec2(tileMapCoordinates) + offset) * (currentOverlay + 1)))), 1.0);
//            if (currentOverlay == 0) debugPixels[i] =  vec4(1.0);
        }
    }

//    for (int i = 0; i < 9; i++) {
//        vec2 minBoundry = debugBoundries[i * 2];
//        vec2 maxBoundry = debugBoundries[i * 2 + 1];
//        if (withinBounds(tileCoordinates, vec2(minBoundry.y, minBoundry.x), vec2(maxBoundry.y, maxBoundry.x))) return debugPixels[i];
//    }



//    vec2[9][MAX_OVERLAYS] neighborTextureCords;
    vec2[OVERLAY_NEIGHBORS_SIZE] neighborTextureCords;

//    vec4[9][MAX_OVERLAYS] pixels;
    vec4[OVERLAY_NEIGHBORS_SIZE] pixels;
    for (int i = 0; i < 9; i++) {
        ivec2 offset = offsets[i];
        for (int currentOverlay = 0; currentOverlay < int(uOverlayAmount); currentOverlay++) {
            int index = getIndex2d(ivec2(i, currentOverlay), OVERLAY_NEIGHBORS_DIMENSIONS);
            vec2 relativeDistance = tileCoordinates - vec2(offset);
            vec2 neighborTexCord = (relativeDistance - uOffset - randomOffsets[i][currentOverlay]) / uScale;
            neighborTextureCords[index] = neighborTexCord;
            pixels[index] = texture(uOverlayTexture, vec3(neighborTexCord, tileIndecies[index]));
        }
        // thought process
        // for instance we have a texture that scaled to 1.2 and offset is -0.15 and the offset is (-1, -1)(left top corner)
        // if our pixel at 0.05, 0.05, then it somehow should be converted to a point within 0..1
        // ig the first thing first we should somehow check te distance to the texture corner of the neighbour, so 0.05 - (-1) = 1.05
        // 1.05 is relative coordinate for our pixel from neighbor corner. Okay but we have -0.15 offset,
        // so the actualy start of the texture should be further and we should reflect it on our relative coordinate like:
        // 1.05 - offset(might include random offset too), so 1.05 - (-0.15) = 1.2. Then divide by scale: 1.2 / scale =
        // = 1.2 / 1.2 = 1
    }

    vec4 outputColor = vec4(-1);
    for (int i = 0; i < 9; i++) {
        ivec2 offset = offsets[i];
        if (!neighborTileStatus[i]) continue;
        for (int currentOverlay = 0; currentOverlay < int(uOverlayAmount); currentOverlay++) {
            int index = getIndex2d(ivec2(i, currentOverlay), OVERLAY_NEIGHBORS_DIMENSIONS);
            if (!withinUnit(neighborTextureCords[index])) continue;
            if (any(lessThan(outputColor, vec4(0)))) {
                outputColor = pixels[index];
            } else {
                outputColor = blendPixels(pixels[index], outputColor);
            }
        }
    }


    if (any(lessThan(outputColor, vec4(0)))) {
        return vec4(-1);
    }

    return outputColor  * uColorTint;
}


void main() {
    //    FragColor = gaussianBlur3x3(vTexCord);
    FragColor = getPixel(vTexCord);
    if (any(lessThan(FragColor, vec4(0.0)))) discard;
//    if (FragColor.a < 0.2) discard;
}