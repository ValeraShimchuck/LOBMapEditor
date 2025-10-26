package ua.valeriishymchuk.lobmapeditor.render.program

import ua.valeriishymchuk.lobmapeditor.render.helper.CurrentGL


interface Program<T, U> {
    val program: Int
    val vao: Int
    val vbo: Int

    fun setUpVBO(ctx: CurrentGL, data: T)

    fun setUpVAO(ctx: CurrentGL)




    fun applyUniform(ctx: CurrentGL, data: U)


    companion object {
        fun compileShader(ctx: CurrentGL, isVertex: Boolean, source: String): Int {
            val shaderType: Int = if (isVertex) CurrentGL.GL_VERTEX_SHADER else CurrentGL.GL_FRAGMENT_SHADER
            val shader = ctx.glCreateShader(shaderType)
            ctx.glShaderSource(shader, 1, arrayOf(source), null)
            ctx.glCompileShader(shader)
            val compilationResultArray: IntArray = IntArray(1)
            ctx.glGetShaderiv(shader, CurrentGL.GL_COMPILE_STATUS, compilationResultArray, 0)
            val isSuccess = compilationResultArray[0] != 0
            if (!isSuccess) {
                val logLengthArray: IntArray = IntArray(1)
                ctx.glGetShaderiv(shader, CurrentGL.GL_INFO_LOG_LENGTH, logLengthArray, 0)
                val logLength = logLengthArray[0]
                val logs: ByteArray = ByteArray(logLength)
                ctx.glGetShaderInfoLog(shader, logLength, null, 0, logs, 0)
                System.err.println("Caught error when compiling shader")
                System.err.println(logs.decodeToString())
                throw IllegalStateException("Failed to compile shader")
            }
            return shader
        }

        fun compileProgram(ctx: CurrentGL, vertexSource: String, fragmentSource: String): Int {
            val vertexShader = compileShader(ctx, true, vertexSource)
            val fragmentShader = compileShader(ctx, false, fragmentSource)

            val program = ctx.glCreateProgram()
            ctx.glAttachShader(program, vertexShader)
            ctx.glAttachShader(program, fragmentShader)
            ctx.glLinkProgram(program)

            val linkingResultArray: IntArray = IntArray(1)
            ctx.glGetProgramiv(program, CurrentGL.GL_LINK_STATUS, linkingResultArray, 0)
            val isSuccess = linkingResultArray[0] != 0
            if (!isSuccess) {
                val logLengthArray: IntArray = IntArray(1)
                ctx.glGetProgramiv(program, CurrentGL.GL_INFO_LOG_LENGTH, logLengthArray, 0)
                val logLength = logLengthArray[0]
                val logs: ByteArray = ByteArray(logLength)
                ctx.glGetProgramInfoLog(program, logLength, null, 0, logs, 0)
                System.err.println("Caught error when linking shader")
                System.err.println(logs.decodeToString())
                throw IllegalStateException("Failed to link program")
            }

            ctx.glDeleteShader(vertexShader)
            ctx.glDeleteShader(fragmentShader)

            return program
        }
    }

}