// ==========================================================================
// MEDIAHUB - CLIENT CONTROLLER SCRIPT (LIGHT THEME)
// Tu dong quet file, cap nhat danh sach phat va xu ly upload qua HTTP POST
// ==========================================================================

let currentAudioUrl = '';
let currentVideoUrl = '';
let currentPdfUrl = '';

document.addEventListener('DOMContentLoaded', () => {
    refreshAll();
    setupUploadHandlers();
});

// 1. Quet toan bo tap tin media va tai lieu tu Server
async function refreshAll() {
    await Promise.all([
        loadMediaFiles(),
        loadDocumentFiles()
    ]);
}

// 2. Nap danh sach audio va video tu /api/files?dir=media
async function loadMediaFiles() {
    try {
        const res = await fetch('/api/files?dir=media&t=' + Date.now(), { cache: 'no-store' });
        if (!res.ok) return;
        const files = await res.json();

        const selectAudio = document.getElementById('selectAudio');
        const selectVideo = document.getElementById('selectVideo');

        const audioFiles = files.filter(f => {
            const n = f.name.toLowerCase();
            return n.endsWith('.mp3') || n.endsWith('.wav') || n.endsWith('.ogg');
        });

        const videoFiles = files.filter(f => {
            const n = f.name.toLowerCase();
            return n.endsWith('.mp4') || n.endsWith('.webm') || n.endsWith('.mkv');
        });

        // Cap nhat dropdown Audio
        if (selectAudio) {
            selectAudio.innerHTML = '';
            if (audioFiles.length === 0) {
                selectAudio.innerHTML = '<option value="">(Khong co file nhac trong media/)</option>';
            } else {
                audioFiles.forEach((f, idx) => {
                    const opt = document.createElement('option');
                    opt.value = f.url;
                    opt.textContent = `${f.name} (${f.sizeText})`;
                    selectAudio.appendChild(opt);
                });
                if (!currentAudioUrl || !audioFiles.some(f => f.url === currentAudioUrl)) {
                    changeAudio(audioFiles[0].url, audioFiles[0].name);
                }
            }
        }

        // Cap nhat dropdown Video
        if (selectVideo) {
            selectVideo.innerHTML = '';
            if (videoFiles.length === 0) {
                selectVideo.innerHTML = '<option value="">(Khong co file video trong media/)</option>';
            } else {
                videoFiles.forEach((f, idx) => {
                    const opt = document.createElement('option');
                    opt.value = f.url;
                    opt.textContent = `${f.name} (${f.sizeText})`;
                    selectVideo.appendChild(opt);
                });
                if (!currentVideoUrl || !videoFiles.some(f => f.url === currentVideoUrl)) {
                    changeVideo(videoFiles[0].url, videoFiles[0].name);
                }
            }
        }
    } catch (e) {
        console.error('Loi khi tai danh sach media:', e);
    }
}

// 3. Nap danh sach tai lieu tu /api/files?dir=documents
async function loadDocumentFiles() {
    try {
        const res = await fetch('/api/files?dir=documents&t=' + Date.now(), { cache: 'no-store' });
        if (!res.ok) return;
        const files = await res.json();

        const selectPdf = document.getElementById('selectPdf');
        const pdfFiles = files.filter(f => f.name.toLowerCase().endsWith('.pdf'));

        if (selectPdf) {
            selectPdf.innerHTML = '';
            if (pdfFiles.length === 0) {
                selectPdf.innerHTML = '<option value="">(Khong co tai lieu PDF trong documents/)</option>';
            } else {
                pdfFiles.forEach(f => {
                    const opt = document.createElement('option');
                    opt.value = f.url;
                    opt.textContent = `${f.name} (${f.sizeText})`;
                    selectPdf.appendChild(opt);
                });
                if (!currentPdfUrl || !pdfFiles.some(f => f.url === currentPdfUrl)) {
                    changePdf(pdfFiles[0].url, pdfFiles[0].name);
                }
            }
        }
    } catch (e) {
        console.error('Loi khi tai danh sach tai lieu:', e);
    }
}

// 4. Chuyen doi bai hat duoc chon
function changeAudio(url, name) {
    if (!url) return;
    currentAudioUrl = url;
    const player = document.getElementById('audioPlayer');
    const lbl = document.getElementById('lblAudioName');
    const btnTab = document.getElementById('btnAudioTab');
    const btnDl = document.getElementById('btnAudioDownload');

    if (lbl) lbl.textContent = name || url.split('/').pop();
    if (player) {
        player.src = url + '?t=' + Date.now();
        player.load();
    }
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name || 'audio.mp3';
    }
}

// 5. Chuyen doi video duoc chon
function changeVideo(url, name) {
    if (!url) return;
    currentVideoUrl = url;
    const player = document.getElementById('videoPlayer');
    const lbl = document.getElementById('lblVideoName');
    const btnTab = document.getElementById('btnVideoTab');
    const btnDl = document.getElementById('btnVideoDownload');

    if (lbl) lbl.textContent = name || url.split('/').pop();
    if (player) {
        player.src = url + '?t=' + Date.now();
        player.load();
    }
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name || 'video.mp4';
    }
}

// 6. Chuyen doi tai lieu PDF duoc chon
function changePdf(url, name) {
    if (!url) return;
    currentPdfUrl = url;
    const frame = document.getElementById('pdfFrame');
    const lbl = document.getElementById('lblPdfName');
    const btnTab = document.getElementById('btnPdfTab');
    const btnDl = document.getElementById('btnPdfDownload');

    if (lbl) lbl.textContent = name || url.split('/').pop();
    if (frame) {
        frame.src = url + '?t=' + Date.now() + '#toolbar=0';
    }
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name || 'sample.pdf';
    }
}

// 7. Xu ly Upload tep tin qua HTTP POST
function setupUploadHandlers() {
    const fileInput = document.getElementById('fileUploadInput');
    const dropZone = document.getElementById('uploadDropZone');
    const statusBox = document.getElementById('uploadStatus');

    if (fileInput) {
        fileInput.addEventListener('change', () => {
            if (fileInput.files.length > 0) {
                performUpload(fileInput.files[0]);
            }
        });
    }

    if (dropZone) {
        dropZone.addEventListener('dragover', (e) => {
            e.preventDefault();
            dropZone.classList.add('dragover');
        });
        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('dragover');
        });
        dropZone.addEventListener('drop', (e) => {
            e.preventDefault();
            dropZone.classList.remove('dragover');
            if (e.dataTransfer.files.length > 0) {
                performUpload(e.dataTransfer.files[0]);
            }
        });
    }
}

async function performUpload(file) {
    const statusBox = document.getElementById('uploadStatus');
    if (!statusBox) return;

    statusBox.style.display = 'block';
    statusBox.className = 'upload-status upload-loading';
    statusBox.textContent = `Dang tai len: ${file.name} (${formatSize(file.size)})...`;

    try {
        const response = await fetch('/api/upload?name=' + encodeURIComponent(file.name), {
            method: 'POST',
            headers: {
                'X-File-Name': encodeURIComponent(file.name),
                'Content-Type': file.type || 'application/octet-stream'
            },
            body: file
        });

        if (response.ok) {
            const data = await response.json();
            statusBox.className = 'upload-status upload-success';
            statusBox.textContent = `[THANH CONG] Da luu: ${data.fileName} vao thu muc uploads/!`;
            setTimeout(() => {
                refreshAll();
            }, 500);
        } else {
            statusBox.className = 'upload-status upload-error';
            statusBox.textContent = `[THAT BAI] Ma loi HTTP ${response.status}`;
        }
    } catch (err) {
        statusBox.className = 'upload-status upload-error';
        statusBox.textContent = `[LOI KET NOI]: ${err.message}`;
    }
}

function formatSize(bytes) {
    if (bytes <= 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    const idx = Math.min(Math.floor(Math.log10(bytes) / Math.log10(1024)), units.length - 1);
    return (bytes / Math.pow(1024, idx)).toFixed(2) + ' ' + units[idx];
}
