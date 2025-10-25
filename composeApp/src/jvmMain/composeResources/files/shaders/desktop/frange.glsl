#version 330 core
#define PI_2 6.28318530718
in vec2 vPos;
in vec4  vColor;
in float vRadius;
in float vInnerRadius;
in float vStartAngle;    // in radians
in float vEndAngle;      // in radians
in vec2  vCenter;

uniform float uThickness = 1.0;
out vec4 FragColor;

float distanceToLine(vec2 p, vec2 a, vec2 b) {
    vec2 ab = b - a;
    vec2 ap = p - a;
    float t = clamp(dot(ap, ab) / dot(ab, ab), 0.0, 1.0);
    vec2 closest = a + t * ab;
    return length(p - closest);
}


void main() {

    vec2 coord  = vPos - vCenter;

    float dist = length(coord);
    float angle = atan(coord.y, coord.x);


    if (angle < 0.0) {
        angle += PI_2;
    }

    bool inAngleRange = angle >= vStartAngle || angle <= vEndAngle;
    bool inRadialRange = dist <= vRadius && dist >= vInnerRadius;
    bool withinMainSector = inAngleRange && inRadialRange;

    vec2 startEdgePoint = vec2(
        vCenter.x + vRadius * cos(vStartAngle),
        vCenter.y + vRadius * sin(vStartAngle)
    );

    vec2 endEdgePoint = vec2(
    vCenter.x + vRadius * cos(vEndAngle),
    vCenter.y + vRadius * sin(vEndAngle)
    );


//    vec2 coord2 = vPos - (vCenter + vec2(2.0, 0.0) );
//    float dist2 = length(coord2);
//    float angle2 = atan(coord2.y, coord2.x);
//
//    if (angle2 < 0.0) {
//        angle2 += PI_2;
//    }
//
//    bool inAngleRange2 = angle2 >= vStartAngle || angle2 <= vEndAngle;
//    bool withinExclusionSector = inAngleRange2 && dist2 <= vRadius && !inRadialRange;


    if (withinMainSector) {
         FragColor = vColor;
    } else {
        float distance = min(
            distanceToLine(vPos, vCenter, startEdgePoint),
            distanceToLine(vPos, vCenter, endEdgePoint)
        );
        if (distance < uThickness && dist <= vRadius) FragColor = vColor;
        else discard;
    }

}