/**
 * Blitz Chart.js — Simple Canvas chart library (zero dependencies)
 * Supports: bar, line, pie charts for admin/merchant dashboards
 */
const Chart = {
  /**
   * Render a chart into a canvas element.
   * @param {string} canvasId - The canvas element ID
   * @param {object} config - { type: 'bar'|'line'|'pie', labels: string[], datasets: [{label, data, color}] }
   * @param {object} options - { height?: number, showLegend?: boolean, yLabel?: string }
   */
  render(canvasId, config, options) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const dpr = window.devicePixelRatio || 1;

    const W = canvas.parentElement.clientWidth - 32;
    const H = options && options.height ? options.height : 280;
    canvas.style.width = W + 'px';
    canvas.style.height = H + 'px';
    canvas.width = W * dpr;
    canvas.height = H * dpr;
    ctx.scale(dpr, dpr);

    const pad = { top: 20, right: 20, bottom: 40, left: 60 };
    const cw = W - pad.left - pad.right;
    const ch = H - pad.top - pad.bottom;

    // Clear
    ctx.clearRect(0, 0, W, H);

    if (config.type === 'pie' || config.type === 'doughnut') {
      this._drawPie(ctx, config, W, H, pad, options);
    } else if (config.type === 'line') {
      this._drawLine(ctx, config, W, H, pad, cw, ch, options);
    } else {
      this._drawBar(ctx, config, W, H, pad, cw, ch, options);
    }
  },

  _drawBar(ctx, config, W, H, pad, cw, ch, options) {
    const { labels, datasets } = config;
    if (!labels || !datasets) return;

    const groups = datasets.length;
    const totalBars = labels.length;
    const groupWidth = cw / totalBars;
    const barWidth = groupWidth * 0.7 / groups;
    const gap = groupWidth * 0.15;

    // Find max value
    let maxVal = 0;
    datasets.forEach(ds => {
      ds.data.forEach(v => { if (v > maxVal) maxVal = v; });
    });
    maxVal = Math.ceil(maxVal * 1.15);
    if (maxVal === 0) maxVal = 10;

    // Y axis
    ctx.strokeStyle = '#E8E8E8';
    ctx.lineWidth = 1;
    ctx.fillStyle = '#999';
    ctx.font = '11px -apple-system, sans-serif';
    ctx.textAlign = 'right';
    const ySteps = 5;
    for (let i = 0; i <= ySteps; i++) {
      const y = pad.top + (ch / ySteps) * i;
      ctx.beginPath();
      ctx.moveTo(pad.left, y);
      ctx.lineTo(W - pad.right, y);
      ctx.stroke();
      const val = maxVal - (maxVal / ySteps) * i;
      ctx.fillText(Math.round(val), pad.left - 8, y + 4);
    }

    // X axis labels
    ctx.textAlign = 'center';
    ctx.fillStyle = '#999';
    labels.forEach((label, i) => {
      const x = pad.left + groupWidth * i + groupWidth / 2;
      ctx.fillText(String(label).substring(0, 6), x, H - pad.bottom + 18);
    });

    // Bars
    datasets.forEach((ds, di) => {
      ctx.fillStyle = ds.color || '#FF5000';
      ds.data.forEach((val, i) => {
        const x = pad.left + groupWidth * i + gap + barWidth * di + (groups > 1 ? barWidth * di : 0);
        const barH = (val / maxVal) * ch;
        const y = pad.top + ch - barH;
        this._roundRect(ctx, x, y, barWidth || groupWidth * 0.6, barH, 4);
        ctx.fill();
      });
    });

    // Legend
    if (options && options.showLegend !== false && datasets.length > 1) {
      this._drawLegend(ctx, datasets, W, pad.top - 5);
    }
  },

  _drawLine(ctx, config, W, H, pad, cw, ch, options) {
    const { labels, datasets } = config;
    if (!labels || !datasets) return;

    let maxVal = 0;
    datasets.forEach(ds => {
      ds.data.forEach(v => { if (v > maxVal) maxVal = v; });
    });
    maxVal = Math.ceil(maxVal * 1.15);
    if (maxVal === 0) maxVal = 10;

    // Grid
    ctx.strokeStyle = '#E8E8E8';
    ctx.lineWidth = 1;
    ctx.fillStyle = '#999';
    ctx.font = '11px -apple-system, sans-serif';
    ctx.textAlign = 'right';
    const ySteps = 5;
    for (let i = 0; i <= ySteps; i++) {
      const y = pad.top + (ch / ySteps) * i;
      ctx.beginPath();
      ctx.moveTo(pad.left, y);
      ctx.lineTo(W - pad.right, y);
      ctx.stroke();
      ctx.fillText(Math.round(maxVal - (maxVal / ySteps) * i), pad.left - 8, y + 4);
    }

    // X labels
    ctx.textAlign = 'center';
    ctx.fillStyle = '#999';
    const step = Math.max(1, Math.floor(labels.length / 8));
    labels.forEach((label, i) => {
      if (i % step === 0 || i === labels.length - 1) {
        const x = pad.left + (cw / (labels.length - 1 || 1)) * i;
        ctx.fillText(String(label).substring(0, 6), x, H - pad.bottom + 18);
      }
    });

    // Line datasets
    datasets.forEach(ds => {
      const color = ds.color || '#FF5000';
      ctx.strokeStyle = color;
      ctx.lineWidth = 2.5;
      ctx.lineJoin = 'round';
      ctx.beginPath();
      ds.data.forEach((val, i) => {
        const x = pad.left + (cw / (ds.data.length - 1 || 1)) * i;
        const y = pad.top + ch - (val / maxVal) * ch;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      });
      ctx.stroke();

      // Dots
      ctx.fillStyle = color;
      ds.data.forEach((val, i) => {
        const x = pad.left + (cw / (ds.data.length - 1 || 1)) * i;
        const y = pad.top + ch - (val / maxVal) * ch;
        ctx.beginPath();
        ctx.arc(x, y, 4, 0, Math.PI * 2);
        ctx.fill();
      });

      // Area fill
      ctx.fillStyle = color + '15';
      ctx.beginPath();
      ds.data.forEach((val, i) => {
        const x = pad.left + (cw / (ds.data.length - 1 || 1)) * i;
        const y = pad.top + ch - (val / maxVal) * ch;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      });
      const lastX = pad.left + cw;
      ctx.lineTo(lastX, pad.top + ch);
      ctx.lineTo(pad.left, pad.top + ch);
      ctx.closePath();
      ctx.fill();
    });

    // Legend
    if (options && options.showLegend !== false && datasets.length > 1) {
      this._drawLegend(ctx, datasets, W, pad.top - 5);
    }
  },

  _drawPie(ctx, config, W, H, pad, options) {
    const { labels, datasets } = config;
    if (!datasets || !datasets[0]) return;
    const data = datasets[0].data;
    const colors = datasets[0].colors || ['#FF5000','#FF7A33','#FF9800','#FFB74D','#FFCC02','#E2231A','#1890FF','#00A854','#722ED1','#EB2F96'];
    const total = data.reduce((a, b) => a + b, 0);
    const cx = W / 2, cy = H / 2;
    const radius = Math.min(W, H) / 2 - 40;

    let startAngle = -Math.PI / 2;
    data.forEach((val, i) => {
      const sliceAngle = (val / total) * Math.PI * 2;
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, radius, startAngle, startAngle + sliceAngle);
      ctx.closePath();
      ctx.fillStyle = colors[i % colors.length];
      ctx.fill();
      ctx.strokeStyle = '#fff';
      ctx.lineWidth = 2;
      ctx.stroke();

      // Label
      const midAngle = startAngle + sliceAngle / 2;
      const lx = cx + Math.cos(midAngle) * (radius + 20);
      const ly = cy + Math.sin(midAngle) * (radius + 20);
      ctx.fillStyle = '#666';
      ctx.font = '11px -apple-system, sans-serif';
      ctx.textAlign = midAngle > Math.PI / 2 || midAngle < -Math.PI / 2 ? 'right' : 'left';
      const pct = Math.round((val / total) * 100);
      if (pct >= 3) {
        ctx.fillText((labels[i] || '') + ' ' + pct + '%', lx, ly);
      }

      startAngle += sliceAngle;
    });
  },

  _drawLegend(ctx, datasets, W, y) {
    let x = W - 20;
    [...datasets].reverse().forEach(ds => {
      ctx.fillStyle = ds.color || '#FF5000';
      this._roundRect(ctx, x - 80, y, 12, 12, 3);
      ctx.fill();
      ctx.fillStyle = '#666';
      ctx.font = '11px -apple-system, sans-serif';
      ctx.textAlign = 'left';
      ctx.fillText(ds.label || '', x - 64, y + 10);
      x -= 120;
    });
  },

  _roundRect(ctx, x, y, w, h, r) {
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + w - r, y);
    ctx.arcTo(x + w, y, x + w, y + r, r);
    ctx.lineTo(x + w, y + h);
    ctx.arcTo(x + w, y + h, x + w - r, y + h, r);
    ctx.lineTo(x + r, y + h);
    ctx.arcTo(x, y + h, x, y + h - r, r);
    ctx.lineTo(x, y + r);
    ctx.arcTo(x, y, x + r, y, r);
    ctx.closePath();
  }
};
