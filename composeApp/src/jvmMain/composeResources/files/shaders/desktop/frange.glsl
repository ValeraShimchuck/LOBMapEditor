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
    bool withinMainSector = inAngleRange && inRadialRange ;

    vec2 startEdgePoint = vec2(
        vCenter.x + vRadius * cos(vStartAngle),
        vCenter.y + vRadius * sin(vStartAngle)
    );

    vec2 endEdgePoint = vec2(
    vCenter.x + vRadius * cos(vEndAngle),
    vCenter.y + vRadius * sin(vEndAngle)
    );


//    FragColor = vec4(angle / PI_2, angle / PI_2, angle / PI_2, 1.0);

    if (withinMainSector) {
         FragColor = vColor;
    } else {
        float distance = min(
            distanceToLine(vPos, vCenter, startEdgePoint),
            distanceToLine(vPos, vCenter, endEdgePoint)
        );
        bool isRange360Deg = abs(vStartAngle - vEndAngle) < 0.1; // error here
        if (distance < uThickness && dist <= vRadius && !isRange360Deg) FragColor = vColor;
        else discard;
    }

}