// fractal-animation.js

// Convierte un color HEX a array [R, G, B]
function hexToRgb(hex) {
    const bigint = parseInt(hex.slice(1), 16);
    return [(bigint >> 16) & 255, (bigint >> 8) & 255, bigint & 255];
}

// Función de interpolación suave
function easeInOutCubic(t) {
    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
}

// Renderiza el fractal en un canvas offscreen y devuelve ImageData
function renderMandelbrot(cx, cy, zoom, palette, w, h) {
    const offCanvas = document.createElement('canvas');
    offCanvas.width = w;
    offCanvas.height = h;
    const ctx = offCanvas.getContext('2d');
    const imageData = ctx.createImageData(w, h);
    const data = imageData.data;
    const maxIter = 200;
    const size = 3.5 / zoom;
    const reMin = cx - size / 2;
    const imMin = cy - size / 2;

    for (let px = 0; px < w; px++) {
        for (let py = 0; py < h; py++) {
            const x0 = reMin + (px * size) / w;
            const y0 = imMin + (py * size) / h;
            let x = 0, y = 0, iter = 0;
            while (x * x + y * y <= 4 && iter < maxIter) {
                const xtemp = x * x - y * y + x0;
                y = 2 * x * y + y0;
                x = xtemp;
                iter++;
            }
            // Color: negro si no escapa, si no un color de la paleta
            const color = iter === maxIter ? [0, 0, 0] : palette[iter % palette.length];
            const idx = (py * w + px) * 4;
            data[idx] = color[0];
            data[idx + 1] = color[1];
            data[idx + 2] = color[2];
            data[idx + 3] = 255;
        }
    }
    ctx.putImageData(imageData, 0, 0);
    return offCanvas;  // devolvemos el canvas offscreen con la imagen
}

// Función principal que inicia la animación
function startAnimation(params) {
    const canvas = document.getElementById('fractalCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;

    // Parámetros iniciales y finales desde el servidor
    const startRe = params.startRe;
    const startIm = params.startIm;
    const startZoom = params.startZoom;
    const targetRe = params.targetRe;
    const targetIm = params.targetIm;
    const targetZoom = params.targetZoom;
    const paletteHex = params.palette;  // array de strings HEX
    const palette = paletteHex.map(hexToRgb);
    const duration = 6000; // 6 segundos

    let startTime = null;

    function animate(timestamp) {
        if (!startTime) startTime = timestamp;
        const elapsed = timestamp - startTime;
        const progress = Math.min(elapsed / duration, 1.0);
        const eased = easeInOutCubic(progress);

        // Interpolar los parámetros actuales
        const currentZoom = startZoom + (targetZoom - startZoom) * eased;
        const currentCx = startRe + (targetRe - startRe) * eased;
        const currentCy = startIm + (targetIm - startIm) * eased;

        // Render a baja resolución (300x200) para mantener fluidez
        const lowRes = renderMandelbrot(currentCx, currentCy, currentZoom, palette, 300, 200);
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.imageSmoothingEnabled = false;  // pixelado artístico
        ctx.drawImage(lowRes, 0, 0, canvas.width, canvas.height);

        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            // Animación terminada: render final en alta resolución
            const finalCanvas = renderMandelbrot(targetRe, targetIm, targetZoom, palette, canvas.width, canvas.height);
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.drawImage(finalCanvas, 0, 0);
            // Mostrar el panel final con el contrato
            showFinalLayout(finalCanvas); // pasamos el canvas para la miniatura
        }
    }

    requestAnimationFrame(animate);
}

function showFinalLayout(finalCanvas) {
    // Ocultar el canvas grande y mostrar el layout final
    document.getElementById('fractalCanvas').style.display = 'none';
    const finalDiv = document.getElementById('finalLayout');
    finalDiv.style.display = 'flex';

    // Colocar la imagen del fractal en el contenedor izquierdo
    const imgElement = document.getElementById('fractalImage');
    imgElement.src = finalCanvas.toDataURL('image/png');

    // El botón de descarga del contrato ya está en el HTML
}