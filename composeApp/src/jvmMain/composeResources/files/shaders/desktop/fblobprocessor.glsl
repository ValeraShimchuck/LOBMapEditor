#version 330 core

in vec2 vTexCord;

out vec4 FragColor;

uniform usampler2D uTileMap;
uniform sampler2DArray uBlobTexture;

uniform vec2 uTileUnit;// (1,1) / map_size_in_tiles
uniform vec4 uColorTint;

bool withinBounds(vec2 toCheck, vec2 minBounds, vec2 maxBounds) {
    return toCheck.x >= minBounds.x && toCheck.x < maxBounds.x && toCheck.y >= minBounds.y && toCheck.y < maxBounds.y;
}

bool withinUnit(vec2 cords) {
    return withinBounds(cords, vec2(0), vec2(1));
}

int checkSet(vec2 offsetCords, int bitToSet) {
    bool withinCords = withinUnit(offsetCords);
    if (!withinCords) return 1 << bitToSet;
    return int(texture(uTileMap, offsetCords).r > 0u) << bitToSet;
}

bool checkMask(int value, int mask) {
    return (value & mask ) == mask;
}

vec4 getPixel(vec2 texCord) {
    uint tileValue = texture(uTileMap, texCord).r;
    bool isSet = tileValue == 1u;
//    bool shouldConnect = tileValue == 2u;
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

    vec4[48] blobTextureValues;
    for (int i = 0; i < 48; i++) {
        blobTextureValues[i] = texture(uBlobTexture, vec3(tileCoordinates, i));
    }

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




    // SIDE             | bit | mask
    // Top              | 0   | 1
    // Right            | 1   | 2
    // Bottom           | 2   | 4
    // Left             | 3   | 8
    // Top-Right        | 4   | 16
    // Top-Left         | 5   | 32
    // Bottom-Right     | 6   | 64
    // Bottom-Left      | 7   | 128

    // special case | bits    | mask
    // All-Sides    | 0 1 2 3 | 15
    // All-Corners  | 4 5 6 7 | 240

    // Case                                                 | side bits       | mask | x y | layer
    // Right-Bottom                                         | 1 2             | 6    | 0 0 | 0
    // Left-Right                                           | 3 1             | 10   | 1 0 | 1
    // Left-Right-Bottom Bottom-Right corners               | 3 1 2 6         | 78   | 2 0 | 2
    // Bottom-Left Bottom-Left corner                       | 2 3 7           | 140  | 3 0 | 3
    // Bottom-Right Bottom-Right corner                     | 2 1 6           | 70   | 4 0 | 4
    // Left-Right-Bottom Bottom-Left Bottom-Right corners   | 3 1 2 7 6       | 206  | 5 0 | 5
    // Left-Right-Bottom Bottom-Left corners                | 3 1 2 7         | 142  | 6 0 | 6
    // Bottom-Left                                          | 2 3             | 12   | 7 0 | 7
    // -------------------------------------------------------------------------
    // Top-Right-Bottom                                     | 0 1 2           | 7    | 0 1 | 8
    // Left-Right-Bottom                                    | 3 1 2           | 14   | 1 1 | 9
    // All-Sides Top-Right corners                          | 0 1 2 3 4       | 31   | 2 1 | 10
    // All-Sides Top-Left Bottom-Right corners              | 0 1 2 3 5 6     | 111  | 3 1 | 11
    // All-Sides Top-Right Bottom-Right Bottom-Left corners | 0 1 2 3 4 6 7   | 223  | 4 1 | 12
    // All-Sides All-Corners                                | 0 1 2 3 4 5 6 7 | 255  | 5 1 | 13
    // Top-Left-Bottom Top-Left Bottom-Left                 | 0 3 2 5 7       | 173  | 6 1 | 14
    // Top-Bottom                                           | 0 2             | 5    | 7 1 | 15
    // -------------------------------------------------------------------------
    // Top-Right-Bottom Bottom-Right corner                 | 0 1 2 6         | 71   | 0 2 | 16
    // All-Sides Bottom-Left corner                         | 0 1 2 3 7       | 143  | 1 2 | 17
    // All-Sides                                            | 0 1 2 3         | 15   | 2 2 | 18
    // All-Sides Top-Right Bottom-Right corners             | 0 1 2 3 4 6     | 95   | 3 2 | 19
    // All-Sides Top-Right Top-Left Bottom-Left corners     | 0 1 2 3 4 5 7   | 191  | 4 2 | 20
    // All-Sides Top-Left Top-Right corners                 | 0 1 2 3 5 4     | 63   | 5 2 | 21
    // Top-Left-Bottom Top-Left corners                     | 0 3 2 5         | 45   | 6 2 | 22
    // Top                                                  | 0               | 1    | 7 2 | 23 -
    // -------------------------------------------------------------------------
    // Top-Right-Bottom Top-Right corner                    | 0 1 2 4         | 23   | 0 3 | 24
    // All Sides Top-Left corner                            | 0 1 2 3 5       | 47   | 1 3 | 25
    // Top-Left-Bottom                                      | 0 3 2           | 13   | 2 3 | 26
    // Top-Right-Bottom Top-Right Bottom-Right corners      | 0 1 2 4 6       | 87   | 3 3 | 27
    // All-Sides Top-Left Bottom-Right Bottom-Left corners  | 0 1 2 3 5 6 7   | 239  | 4 3 | 28
    // All-Sides Bottom-Right Bottom-Left corners           | 0 1 2 3 6 7     | 207  | 5 3 | 29
    // Top-Left-Bottom Bottom-Left corners                  | 0 3 2 7         | 141  | 6 3 | 30
    // Bottom                                               | 2               | 4    | 7 3 | 31 -
    // -------------------------------------------------------------------------
    // Top-Right                                            | 0 1             | 3    | 0 4 | 32
    // Left-Top-Right                                       | 0 1 3           | 11   | 1 4 | 33
    // All-Sides Bottom-Right corner                        | 0 1 2 3 6       | 79   | 2 4 | 34
    // All-Sides Bottom-Left Top-Right corners              | 0 1 2 3 7 4     | 159  | 3 4 | 35
    // All-Sides Top-Left Top-Right Bottom-Right corners    | 0 1 2 3 5 4 6   | 127  | 4 4 | 36
    // All-Sides All-Corners                                | 0 1 2 3 4 5 6 7 | 255  | 5 4 | 37
    // All-Sides Top-Left Bottom-Left corners               | 0 1 2 3 5 7     | 175  | 6 4 | 38
    // Top-Left                                             | 0 3             | 9    | 7 4 | 39
    // -------------------------------------------------------------------------
    // None                                                 | none            | 0    | 0 5 | 40
    // Right                                                | 1               | 2    | 1 5 | 41 -
    // Left-Top-Right Top-Right corner                      | 0 1 3 4         | 27   | 2 5 | 42
    // Top-Left Top-Left corner                             | 0 3 5           | 41   | 3 5 | 43
    // Top-Right Top-Right corner                           | 0 1 4           | 19   | 4 5 | 44
    // Left-Top-Right Top-Left Top-Right coners             | 0 1 3 4 5       | 59   | 5 5 | 45
    // Left-Top-Right Top-Left corner                       | 0 1 3 5         | 43   | 6 5 | 46
    // Left                                                 | 3               | 8    | 7 5 | 47 -

    // x is mask and y is layer
    ivec2[48] blobMasks = ivec2[48](
    ivec2(0, 40),   // None
    ivec2(1, 23),   // Top 1
    ivec2(2, 41),   // Right 1
    ivec2(4, 31),   // Bottom 1
    ivec2(8, 47),   // Left 1
    ivec2(3, 32),   // Top-Right 2
    ivec2(5, 15),   // Top-Bottom 2
    ivec2(6, 0),    // Right-Bottom 2
    ivec2(9, 39),   // Top-Left 2
    ivec2(10, 1),   // Left-Right 2
    ivec2(12, 7),   // Bottom-Left 2
    ivec2(19, 44),  // Top-Right Top-Right corner 3
    ivec2(41, 43),  // Top-Left Top-Left corner 3
    ivec2(70, 4),   // Bottom-Right Bottom-Right corner 3
    ivec2(140, 3),  // Bottom-Left Bottom-Left corner 3
    ivec2(7, 8),    // Top-Right-Bottom 3
    ivec2(11, 33),  // Left-Top-Right 3
    ivec2(13, 26),  // Top-Left-Bottom 3
    ivec2(14, 9),   // Left-Right-Bottom 3
    ivec2(23, 24),  // Top-Right-Bottom Top-Right corner 4
    ivec2(27, 42),  // Left-Top-Right Top-Right corner 4
    ivec2(43, 46),  // Left-Top-Right Top-Left corner 4
    ivec2(45, 22),  // Top-Left-Bottom Top-Left corners 4
    ivec2(71, 16),  // Top-Right-Bottom Bottom-Right corner 4
    ivec2(78, 2),   // Left-Right-Bottom Bottom-Right corners 4
    ivec2(141, 30), // Top-Left-Bottom Bottom-Left corners 4
    ivec2(142, 6),  // Left-Right-Bottom Bottom-Left corners 4
    ivec2(15, 18),  // All-Sides 4
    ivec2(87, 27),  // Top-Right-Bottom Top-Right Bottom-Right corners 5
    ivec2(173, 14), // Top-Left-Bottom Top-Left Bottom-Left 5
    ivec2(206, 5),  // Left-Right-Bottom Bottom-Left Bottom-Right corners 5
    ivec2(143, 17), // All-Sides Bottom-Left corner 5
    ivec2(31, 10),  // All-Sides Top-Right corners 5
    ivec2(79, 34),  // All-Sides Bottom-Right corner 5
    ivec2(47, 25),  // All-Sides Top-Left corner 5
    ivec2(59, 45),  // Left-Top-Right Top-Left Top-Right coners   5
    ivec2(159, 35), // All-Sides Bottom-Left Top-Right corners 6
    ivec2(175, 38), // All-Sides Top-Left Bottom-Left corners 6
    ivec2(207, 29), // All-Sides Bottom-Right Bottom-Left corners 6
    ivec2(63, 21),  // All-Sides Top-Left Top-Right corners 6
    ivec2(95, 19),  // All-Sides Top-Right Bottom-Right corners 6
    ivec2(111, 11), // All-Sides Top-Left Bottom-Right corners 6
    ivec2(191, 20), // All-Sides Top-Left Top-Right Bottom-Left corners 7
    ivec2(223, 12), // All-Sides Top-Right Bottom-Right Bottom-Left corners 7
    ivec2(239, 28), // All-Sides Top-Left Bottom-Right Bottom-Left corners 7
    ivec2(127, 36), // All-Sides Top-Left Top-Right Bottom-Right corners 7
    ivec2(255, 13), // All-Sides All-Corners 8
    ivec2(255, 37) // All-Sides All-Corners 8
    );

    if (!isSet) return vec4(-1);

    int blobIndex = 0;
    for (int i = 0; i < 48; i++) {
        ivec2 currentBlobMask = blobMasks[i];
        if (checkMask(mask, currentBlobMask.x)) blobIndex = currentBlobMask.y;
    }
//    return blobTextureValues[blobIndex] * uColorTint;
    return blobTextureValues[blobIndex];

}


void main() {
    //    FragColor = gaussianBlur3x3(vTexCord);
    FragColor = getPixel(vTexCord);
    if (any(lessThan(FragColor, vec4(0.0)))) discard;
}