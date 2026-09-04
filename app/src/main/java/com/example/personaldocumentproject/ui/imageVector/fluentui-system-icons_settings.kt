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

val FluentuiSystemIconsSettingsSolid: ImageVector
    get() {
        if (_FluentuiSystemIconsSettings != null) return _FluentuiSystemIconsSettings!!
        
        _FluentuiSystemIconsSettings = ImageVector.Builder(
            name = "settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black)
            ) {
                moveTo(12.0122f, 2.25f)
                curveTo(12.7462f, 2.25846f, 13.4773f, 2.34326f, 14.1937f, 2.50304f)
                curveTo(14.5064f, 2.57279f, 14.7403f, 2.83351f, 14.7758f, 3.15196f)
                lineTo(14.946f, 4.67881f)
                curveTo(15.0231f, 5.37986f, 15.615f, 5.91084f, 16.3206f, 5.91158f)
                curveTo(16.5103f, 5.91188f, 16.6979f, 5.87238f, 16.8732f, 5.79483f)
                lineTo(18.2738f, 5.17956f)
                curveTo(18.5651f, 5.05159f, 18.9055f, 5.12136f, 19.1229f, 5.35362f)
                curveTo(20.1351f, 6.43464f, 20.8889f, 7.73115f, 21.3277f, 9.14558f)
                curveTo(21.4223f, 9.45058f, 21.3134f, 9.78203f, 21.0564f, 9.9715f)
                lineTo(19.8149f, 10.8866f)
                curveTo(19.4607f, 11.1468f, 19.2516f, 11.56f, 19.2516f, 11.9995f)
                curveTo(19.2516f, 12.4389f, 19.4607f, 12.8521f, 19.8157f, 13.1129f)
                lineTo(21.0582f, 14.0283f)
                curveTo(21.3153f, 14.2177f, 21.4243f, 14.5492f, 21.3297f, 14.8543f)
                curveTo(20.8911f, 16.2685f, 20.1377f, 17.5649f, 19.1261f, 18.6461f)
                curveTo(18.9089f, 18.8783f, 18.5688f, 18.9483f, 18.2775f, 18.8206f)
                lineTo(16.8712f, 18.2045f)
                curveTo(16.4688f, 18.0284f, 16.0068f, 18.0542f, 15.6265f, 18.274f)
                curveTo(15.2463f, 18.4937f, 14.9933f, 18.8812f, 14.945f, 19.3177f)
                lineTo(14.7759f, 20.8444f)
                curveTo(14.741f, 21.1592f, 14.5122f, 21.4182f, 14.204f, 21.4915f)
                curveTo(12.7556f, 21.8361f, 11.2465f, 21.8361f, 9.79803f, 21.4915f)
                curveTo(9.48991f, 21.4182f, 9.26105f, 21.1592f, 9.22618f, 20.8444f)
                lineTo(9.05736f, 19.32f)
                curveTo(9.00777f, 18.8843f, 8.75434f, 18.498f, 8.37442f, 18.279f)
                curveTo(7.99451f, 18.06f, 7.5332f, 18.0343f, 7.1322f, 18.2094f)
                lineTo(5.72557f, 18.8256f)
                curveTo(5.43422f, 18.9533f, 5.09403f, 18.8833f, 4.87678f, 18.6509f)
                curveTo(3.86462f, 17.5685f, 3.11119f, 16.2705f, 2.6732f, 14.8548f)
                curveTo(2.57886f, 14.5499f, 2.68786f, 14.2186f, 2.94485f, 14.0293f)
                lineTo(4.18818f, 13.1133f)
                curveTo(4.54232f, 12.8531f, 4.75147f, 12.4399f, 4.75147f, 12.0005f)
                curveTo(4.75147f, 11.561f, 4.54232f, 11.1478f, 4.18771f, 10.8873f)
                lineTo(2.94516f, 9.97285f)
                curveTo(2.6878f, 9.78345f, 2.5787f, 9.45178f, 2.67337f, 9.14658f)
                curveTo(3.11212f, 7.73215f, 3.86594f, 6.43564f, 4.87813f, 5.35462f)
                curveTo(5.09559f, 5.12236f, 5.43594f, 5.05259f, 5.72724f, 5.18056f)
                lineTo(7.12762f, 5.79572f)
                curveTo(7.53056f, 5.97256f, 7.9938f, 5.94585f, 8.37577f, 5.72269f)
                curveTo(8.75609f, 5.50209f, 9.00929f, 5.11422f, 9.05817f, 4.67764f)
                lineTo(9.22824f, 3.15196f)
                curveTo(9.26376f, 2.83335f, 9.49786f, 2.57254f, 9.8108f, 2.50294f)
                curveTo(10.5281f, 2.34342f, 11.26f, 2.25865f, 12.0122f, 2.25f)
                close()
                moveTo(11.9997f, 8.99995f)
                curveTo(10.3428f, 8.99995f, 8.9997f, 10.3431f, 8.9997f, 12f)
                curveTo(8.9997f, 13.6568f, 10.3428f, 15f, 11.9997f, 15f)
                curveTo(13.6565f, 15f, 14.9997f, 13.6568f, 14.9997f, 12f)
                curveTo(14.9997f, 10.3431f, 13.6565f, 8.99995f, 11.9997f, 8.99995f)
                close()
            }
        }.build()
        
        return _FluentuiSystemIconsSettings!!
    }

private var _FluentuiSystemIconsSettings: ImageVector? = null