#version 330 core
layout (location = 0) in vec2 aPos;


uniform mat4 uMVP;
uniform vec2 uMapSize; // in pixels

out vec2 vTexCord;

void main() {
    gl_Position = uMVP * vec4(aPos, 0.0, 1.0);

    // we have UDC in gl_Position [-1..1]
    // but we need [0..1] for texcoord. Also the y should start from the top.

    // so for x it will be just: (x + 1) / 2
    // but for y its kinda complicated. ig this should work (y * -1 + 1) / 2
    //vec2 mapSize = (vec2(1, 1) / uTileUnit);

//    vTexCord = vec2(gl_Position.x + 1, gl_Position.y * -1 + 1 ) / 2;
    vTexCord = aPos / uMapSize;
}