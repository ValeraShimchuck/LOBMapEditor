#version 330 core
in vec2 vTexCoord;

out vec4 FragColor;

uniform bool uDrawMask;
uniform bool uDrawOverlay;

uniform vec4 uMaskColor;
uniform sampler2D uMask;
uniform sampler2D uOverlay;



vec4 blendPixels(vec4 src, vec4 dst) {
    vec4 preProcessed = (src * src.a) + (dst * (1.0 - src.a));
    return vec4(preProcessed.rgb, max(src.a, dst.a));
}

//vec4 blendPixels(vec4 src, vec4 dst) {
//    // Calculate overlay blend for RGB components
//    vec3 overlay = vec3(0.0);
//    overlay.r = (dst.r < 0.5) ? (2.0 * src.r * dst.r) : (1.0 - 2.0 * (1.0 - src.r) * (1.0 - dst.r));
//    overlay.g = (dst.g < 0.5) ? (2.0 * src.g * dst.g) : (1.0 - 2.0 * (1.0 - src.g) * (1.0 - dst.g));
//    overlay.b = (dst.b < 0.5) ? (2.0 * src.b * dst.b) : (1.0 - 2.0 * (1.0 - src.b) * (1.0 - dst.b));
//
//    // Blend using source alpha
//    vec3 resultRGB = mix(dst.rgb, overlay, src.a);
//    float resultAlpha = src.a + dst.a * (1.0 - src.a);
//
//    return vec4(resultRGB, resultAlpha);
//}

void main() {
//    FragColor = vec4(1.0, 0.0, 0.0, 1.0); // Red color
    FragColor = vec4(-1); // ignore the default value, its fine

    if (uDrawMask) FragColor = texture(uMask, vTexCoord) * uMaskColor;
    if (uDrawOverlay && uDrawMask) {
        FragColor = blendPixels(texture(uOverlay, vTexCoord), FragColor);
        if (FragColor.a > 0.0) {
            FragColor = vec4(FragColor.rgb, uMaskColor.a);
        }
//        FragColor = vec4(FragColor.rgb, uMaskColor.a);
    }
    else if (uDrawOverlay) {
        FragColor = texture(uOverlay, vTexCoord);
//        FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    }


    if (any(lessThan(FragColor, vec4(0)))) discard;

}