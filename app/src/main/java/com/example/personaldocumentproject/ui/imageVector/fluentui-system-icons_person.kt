package com.example.personaldocumentproject.ui.imageVector/*
MIT License

Copyright (c) 2020 Microsoft Corporation

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FluentuiSystemIconsPersonSolid: ImageVector
    get() {
        if (_FluentuiSystemIconsPerson != null) return _FluentuiSystemIconsPerson!!
        
        _FluentuiSystemIconsPerson = ImageVector.Builder(
            name = "person",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(17.7545f, 14.0002f)
                curveTo(18.9966f, 14.0002f, 20.0034f, 15.007f, 20.0034f, 16.2491f)
                verticalLineTo(17.1675f)
                curveTo(20.0034f, 17.7409f, 19.8242f, 18.2999f, 19.4908f, 18.7664f)
                curveTo(17.9449f, 20.9296f, 15.4206f, 22.0013f, 12.0004f, 22.0013f)
                curveTo(8.5794f, 22.0013f, 6.05643f, 20.9292f, 4.51427f, 18.7648f)
                curveTo(4.18231f, 18.2989f, 4.00391f, 17.7411f, 4.00391f, 17.169f)
                verticalLineTo(16.2491f)
                curveTo(4.00391f, 15.007f, 5.01076f, 14.0002f, 6.25278f, 14.0002f)
                horizontalLineTo(17.7545f)
                close()
                moveTo(12.0004f, 2.00488f)
                curveTo(14.7618f, 2.00488f, 17.0004f, 4.24346f, 17.0004f, 7.00488f)
                curveTo(17.0004f, 9.76631f, 14.7618f, 12.0049f, 12.0004f, 12.0049f)
                curveTo(9.23894f, 12.0049f, 7.00036f, 9.76631f, 7.00036f, 7.00488f)
                curveTo(7.00036f, 4.24346f, 9.23894f, 2.00488f, 12.0004f, 2.00488f)
                close()
            }
        }.build()
        
        return _FluentuiSystemIconsPerson!!
    }

private var _FluentuiSystemIconsPerson: ImageVector? = null