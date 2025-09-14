#version 330 core

uniform sampler2D pointTexture;
uniform int totalPoints;
in vec2 vPosition;

out vec4 fragColor;


void main() {
    float minDist = 1e20;
    int closestType = 0; // 0 = red, 1 = blue
    if (totalPoints == 0) discard;
    for(int i = 0; i < totalPoints; i++) {
        // Read point data from texture
        vec4 pointData = texelFetch(pointTexture, ivec2(i, 0), 0);
        vec2 pointPos = pointData.xy;
        int pointType = int(pointData.z);

        float dist = distance(vPosition, pointPos);
        if(dist < minDist) {
            minDist = dist;
            closestType = pointType;
        }
    }

    fragColor = (closestType == 0) ? vec4(1, 0, 0, 0.2) : vec4(0, 0, 1, 0.2);
}