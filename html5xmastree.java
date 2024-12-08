<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Canvas Christmas Scene</title>
    <style>
        body {
            margin: 0;
            background: #000;  /* Base background in case canvas doesn't cover entire viewport */
            overflow: hidden;  /* Prevent scrollbars due to potential resize shifts */
            font-family: sans-serif;
        }
        canvas {
            display: block; /* Ensures canvas fills the viewport horizontally */
        }
    </style>
</head>
<body>
<canvas id="scene"></canvas>
<script>
/*

        The code below creates a dark-themed holiday scene using HTML5 Canvas. It includes:
        - A starry night sky with simple twinkling stars implemented via opacity oscillation.
        - A stylized Christmas tree drawn using overlapping triangles, a trunk, ornaments, and a star on top.
        - Falling snow simulation with randomized flake positions and velocities.

        From a security standpoint:
        1. The code does not handle any external input, making it less susceptible to injection attacks (XSS, CSRF).
        2. No dynamic external resources are loaded, reducing risks associated with loading malicious third-party scripts.
        3. The code does not store or retrieve user data, meaning no sensitive information is exposed and no authentication/authorization logic is present.
        4. Since no user-generated content is processed, DOM-based injection vectors are minimal. The canvas drawing commands are static and use controlled parameters.
        5. RequestAnimationFrame and Canvas API calls are standard browser functionalities and do not introduce new security risks.
        6. As a static script, code review confirms that no hidden network requests or data exfiltration occur.

        From a technical and performance standpoint:
        1. requestAnimationFrame is used for smooth rendering, reducing CPU/GPU overhead compared to setInterval.
        2. The number of stars, snowflakes, and ornaments can be adjusted to control performance. Excessive entities may cause slower rendering on low-end devices, but this is a performance rather than a security issue.
        3. Canvas resolution dynamically adjusts on window resize. Memory usage is tied to canvas size, making potential DoS from window resizing negligible in practice since this is a local code snippet.
        4. Complexity is kept at a moderate level. For a highly complex scene, consider quadtree optimizations or other spatial partitioning strategies to reduce frame computation time.

*/

(function() {
        "use strict";

        var canvas = document.getElementById("scene");
        var ctx = canvas.getContext("2d");

        var W, H;
        function resizeCanvas() {
                W = window.innerWidth;
                H = window.innerHeight;
                canvas.width = W;
                canvas.height = H;
        }
        window.addEventListener('resize', resizeCanvas, false);
        resizeCanvas();

        // Configuration for starry background
        // Stars are placed randomly in the top half of the screen.
        // Each star twinkles by changing its alpha value over time.
        var starCount = 200;
        var stars = [];
        for (var i = 0; i < starCount; i++) {
                stars.push({
                        x: Math.random() * W,
                        y: Math.random() * H * 0.5,
                        radius: Math.random() * 1.2,
                        alpha: Math.random(),
                        twinkleSpeed: 0.005 + Math.random() * 0.015
                });
        }

        // Snow configuration
        // Snowflakes fall from top to bottom with slight horizontal drift.
        // Once a flake falls beyond the bottom, it is repositioned at top with a random x.
        var snowCount = 300;
        var snowflakes = [];
        for (var j = 0; j < snowCount; j++) {
                snowflakes.push({
                        x: Math.random() * W,
                        y: Math.random() * H,
                        r: 1 + Math.random() * 2,
                        dx: (Math.random() - 0.5) * 0.5,
                        dy: 0.8 + Math.random() * 0.7
                });
        }

        // Tree configuration
        // Tree is constructed by stacking 3 triangular segments and a trunk.
        // Top: smaller width, bottom: larger width.
        var treeBaseX = W * 0.5;
        var treeBaseY = H * 0.75;
        var treeWidth = Math.min(W * 0.2, 200);
        var treeHeight = treeWidth * 2;

        // Ornaments configuration
        // Ornaments are small colored circles placed randomly around the tree area.
        var ornamentCount = 30;
        var ornaments = [];
        var ornamentColors = ["red", "blue", "yellow", "orange", "magenta", "cyan"];
        for (var k = 0; k < ornamentCount; k++) {
                ornaments.push({
                        x: treeBaseX + (Math.random() - 0.5) * treeWidth * 0.8,
                        y: treeBaseY - Math.random() * treeHeight * 0.8,
                        r: 3 + Math.random() * 3,
                        color: ornamentColors[(Math.random() * ornamentColors.length) | 0]
                });
        }

        // Draws twinkling stars
        function drawStars() {
                for (var i = 0; i < starCount; i++) {
                        var s = stars[i];
                        // Update star alpha for twinkling effect
                        s.alpha += s.twinkleSpeed;
                        if (s.alpha > 1 || s.alpha < 0) s.twinkleSpeed = -s.twinkleSpeed;

                        ctx.globalAlpha = Math.abs(s.alpha);
                        ctx.beginPath();
                        ctx.arc(s.x, s.y, s.radius, 0, Math.PI * 2, false);
                        ctx.fillStyle = "#FFF";
                        ctx.fill();
                }
                ctx.globalAlpha = 1.0;
        }

        // Draws the Christmas tree as stacked green triangles plus a trunk
        // and a star at the top. The star is drawn using a polygon technique.
        function drawTree() {
                ctx.fillStyle = "#0A5311";
                ctx.strokeStyle = "#0A5311";

                var segmentHeight = treeHeight / 3;
                for (var n = 0; n < 3; n++) {
                        var bottomY = treeBaseY - n * segmentHeight;
                        var topY = bottomY - segmentHeight;
                        var baseW = treeWidth - n * (treeWidth * 0.3);
                        ctx.beginPath();
                        ctx.moveTo(treeBaseX - baseW * 0.5, bottomY);
                        ctx.lineTo(treeBaseX, topY);
                        ctx.lineTo(treeBaseX + baseW * 0.5, bottomY);
                        ctx.closePath();
                        ctx.fill();
                }

                // Trunk in a simple rectangle
                var trunkWidth = treeWidth * 0.1;
                var trunkHeight = treeHeight * 0.1;
                ctx.fillStyle = "#6B4D1E";
                ctx.fillRect(treeBaseX - trunkWidth * 0.5, treeBaseY, trunkWidth, trunkHeight);

                // Star at top (5 spikes)
                drawStar(treeBaseX, treeBaseY - treeHeight - 10, 5, 10, 4.5);
        }

        // Draws a star shape for the tree top
        // Uses standard polygon drawing method with alternating inner/outer radii.
        function drawStar(cx, cy, spikes, outerRadius, innerRadius) {
                ctx.fillStyle = "yellow";
                ctx.beginPath();
                var rot = Math.PI / 2 * 3;
                var x = cx;
                var y = cy;
                var step = Math.PI / spikes;

                for (var i = 0; i < spikes; i++) {
                        x = cx + Math.cos(rot) * outerRadius;
                        y = cy + Math.sin(rot) * outerRadius;
                        ctx.lineTo(x, y);
                        rot += step;

                        x = cx + Math.cos(rot) * innerRadius;
                        y = cy + Math.sin(rot) * innerRadius;
                        ctx.lineTo(x, y);
                        rot += step;
                }
                ctx.closePath();
                ctx.fill();
        }

        // Draw ornaments as small colored dots
        // Checks their position to ensure they appear within tree-like bounds.
        function drawOrnaments() {
                for (var i = 0; i < ornamentCount; i++) {
                        var o = ornaments[i];
                        // Simple heuristic to ensure it lies where the tree is
                        if (o.y < treeBaseY && o.y > treeBaseY - treeHeight &&
                            Math.abs(o.x - treeBaseX) < treeWidth * 0.6) {
                                ctx.beginPath();
                                ctx.arc(o.x, o.y, o.r, 0, Math.PI * 2, false);
                                ctx.fillStyle = o.color;
                                ctx.fill();
                        }
                }
        }

        // Draw and animate snowflakes
        // Update their positions, reset if they move off the bottom.
        function drawSnow() {
                ctx.fillStyle = "#FFF";
                for (var i = 0; i < snowCount; i++) {
                        var flake = snowflakes[i];
                        ctx.beginPath();
                        ctx.arc(flake.x, flake.y, flake.r, 0, Math.PI * 2, false);
                        ctx.fill();
                        flake.x += flake.dx;
                        flake.y += flake.dy;
                        // Reset flakes when they go beyond bottom
                        if (flake.y > H) {
                                flake.y = -flake.r;
                                flake.x = Math.random() * W;
                        }
                }
        }

        // Main animation loop
        // Clears the canvas each frame, redraws background, stars, tree, ornaments, and snow.
        // Uses requestAnimationFrame for smoother visuals and optimized CPU usage.
        function animate() {
                // Clear the entire canvas
                ctx.clearRect(0, 0, W, H);

                // Dark gradient background
                var grad = ctx.createLinearGradient(0, 0, 0, H);
                grad.addColorStop(0, "#000010");
                grad.addColorStop(1, "#001020");
                ctx.fillStyle = grad;
                ctx.fillRect(0, 0, W, H);

                drawStars();
                drawTree();
                drawOrnaments();
                drawSnow();

                requestAnimationFrame(animate);
        }

        animate();
})();
</script>
</body>
</html>
