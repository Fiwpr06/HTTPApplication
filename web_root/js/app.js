// ==========================================================================
// MEDIAHUB - CLIENT APPLICATION CONTROLLER (REFACTORED MODULAR ARCHITECTURE)
// Ho tro tab chuyen huong, phat nhac, xem video, xem PDF & Word (.docx)
// ==========================================================================

let activeTab = 'documents';
let currentAudioUrl = '';
let currentVideoUrl = '';
let currentDocUrl = '';
let documentsData = [];
let mediaData = [];

document.addEventListener('DOMContentLoaded', () => {
    setupTabNavigation();
    setupUploadHandlers();
    refreshAll();
});

// 1. Chuyen doi Tab giao dien (Navigation)
function setupTabNavigation() {
    const navButtons = document.querySelectorAll('.nav-tab-btn');
    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabName = btn.getAttribute('data-tab');
            switchTab(tabName);
        });
    });
}

function switchTab(tabName) {
    activeTab = tabName;

    // Cap nhat active tren thanh Tabs
    document.querySelectorAll('.nav-tab-btn').forEach(b => {
        b.classList.toggle('active', b.getAttribute('data-tab') === tabName);
    });

    // Cap nhat hien thi panel tuong ung
    document.querySelectorAll('.tab-panel').forEach(p => {
        p.classList.toggle('active', p.id === 'tab-' + tabName);
    });
}

// 2. Nap toan bo du lieu tu Server
async function refreshAll() {
    await Promise.all([
        loadDocuments(),
        loadMedia()
    ]);
}

// 3. Quan ly Tai Lieu (PDF, DOCX, TXT)
async function loadDocuments() {
    try {
        const res = await fetch('/api/files?dir=documents&t=' + Date.now(), { cache: 'no-store' });
        if (!res.ok) return;
        documentsData = await res.json();

        renderDocumentList();

        // Chon mac dinh tai lieu dau tien neu chua chon
        if (documentsData.length > 0 && (!currentDocUrl || !documentsData.some(d => d.url === currentDocUrl))) {
            selectDocument(documentsData[0].url, documentsData[0].name);
        }
    } catch (e) {
        console.error('Loi khi tai danh sach tai lieu:', e);
    }
}

function renderDocumentList(filterText = '') {
    const listContainer = document.getElementById('docListContainer');
    if (!listContainer) return;

    listContainer.innerHTML = '';
    const filtered = documentsData.filter(d => d.name.toLowerCase().includes(filterText.toLowerCase()));

    if (filtered.length === 0) {
        listContainer.innerHTML = '<div class="empty-state">Khong tim thay tai lieu phu hop.</div>';
        return;
    }

    filtered.forEach(doc => {
        const isDocx = doc.name.toLowerCase().endsWith('.docx') || doc.name.toLowerCase().endsWith('.doc');
        const isPdf = doc.name.toLowerCase().endsWith('.pdf');
        const badgeClass = isDocx ? 'badge-docx' : (isPdf ? 'badge-pdf' : 'badge-file');
        const badgeText = isDocx ? 'DOCX' : (isPdf ? 'PDF' : 'DOC');

        const item = document.createElement('div');
        item.className = 'doc-list-item' + (doc.url === currentDocUrl ? ' active' : '');
        item.onclick = () => selectDocument(doc.url, doc.name);

        item.innerHTML = `
            <div class="doc-item-badge ${badgeClass}">${badgeText}</div>
            <div class="doc-item-info">
                <div class="doc-item-name" title="${doc.name}">${doc.name}</div>
                <div class="doc-item-meta">${doc.sizeText}</div>
            </div>
        `;
        listContainer.appendChild(item);
    });
}

function filterDocuments(keyword) {
    renderDocumentList(keyword);
}

async function selectDocument(url, name) {
    currentDocUrl = url;
    renderDocumentList();

    const titleEl = document.getElementById('selectedDocTitle');
    const badgeEl = document.getElementById('selectedDocBadge');
    const btnTab = document.getElementById('btnDocTab');
    const btnDl = document.getElementById('btnDocDownload');
    const pdfFrame = document.getElementById('pdfFrame');
    const docxContainer = document.getElementById('docxViewerContainer');
    const previewBody = document.querySelector('.doc-preview-body');

    if (titleEl) titleEl.textContent = name;
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name;
    }

    // Cuộn khung xem tài liệu về đầu trang
    if (previewBody) {
        previewBody.scrollTop = 0;
    }

    const lower = name.toLowerCase();
    const isDocx = lower.endsWith('.docx') || lower.endsWith('.doc');

    if (isDocx) {
        if (badgeEl) {
            badgeEl.className = 'badge-tag badge-docx';
            badgeEl.textContent = 'Word Document (DOCX High-Fidelity)';
        }
        if (pdfFrame) pdfFrame.style.display = 'none';
        if (docxContainer) {
            docxContainer.style.display = 'flex';
            docxContainer.innerHTML = '<div class="doc-loading"><span class="dot-pulse"></span> Đang nạp và định dạng từng trang tài liệu Word (.docx)...</div>';
            try {
                const response = await fetch(url, { cache: 'no-store' });
                if (!response.ok) {
                    throw new Error('Máy chủ phản hồi HTTP ' + response.status);
                }
                const arrayBuffer = await response.arrayBuffer();

                if (window.docx && typeof window.docx.renderAsync === 'function') {
                    docxContainer.innerHTML = '';
                    const renderOptions = {
                        className: 'docx',
                        inWrapper: true,
                        ignoreWidth: false,
                        ignoreHeight: false,
                        ignoreFonts: false,
                        breakPages: true,
                        ignoreLastRenderedPageBreak: false,
                        experimental: true,
                        trimXmlDeclaration: true,
                        useBase64URL: true,
                        renderHeaders: true,
                        renderFooters: true,
                        renderFootnotes: true,
                        renderEndnotes: true
                    };
                    await window.docx.renderAsync(arrayBuffer, docxContainer, null, renderOptions);
                } else {
                    docxContainer.innerHTML = '<div class="doc-error">Thư viện docx-preview chưa sẵn sàng trên trình duyệt. Bạn có thể bấm [Tải Về] để mở tệp.</div>';
                }
            } catch (err) {
                docxContainer.innerHTML = `<div class="doc-error">Lỗi khi đọc tài liệu DOCX: ${err.message}</div>`;
            }
        }
    } else {
        // PDF hoac dinh dang khac
        if (badgeEl) {
            badgeEl.className = 'badge-tag badge-pdf';
            badgeEl.textContent = 'Adobe PDF (Native Embed)';
        }
        if (docxContainer) docxContainer.style.display = 'none';
        if (pdfFrame) {
            pdfFrame.style.display = 'block';
            pdfFrame.src = url + '?t=' + Date.now() + '#toolbar=1';
        }
    }
}

// 4. Quan ly Âm Nhạc (Audio Player & Playlist)
async function loadMedia() {
    try {
        const res = await fetch('/api/files?dir=media&t=' + Date.now(), { cache: 'no-store' });
        if (!res.ok) return;
        mediaData = await res.json();

        renderAudioPlaylist();
        renderVideoGallery();
    } catch (e) {
        console.error('Loi khi tai media:', e);
    }
}

function renderAudioPlaylist() {
    const audioFiles = mediaData.filter(f => {
        const n = f.name.toLowerCase();
        return n.endsWith('.mp3') || n.endsWith('.wav') || n.endsWith('.ogg');
    });

    const tbody = document.getElementById('audioPlaylistTbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    if (audioFiles.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Khong co file nhac trong media/</td></tr>';
        return;
    }

    audioFiles.forEach((f, idx) => {
        const tr = document.createElement('tr');
        const isCurrent = f.url === currentAudioUrl;
        if (isCurrent) tr.className = 'playing-row';

        tr.innerHTML = `
            <td style="width: 40px; text-align: center;">${idx + 1}</td>
            <td>
                <div style="font-weight: 600; color: #1E3A8A;">${f.name}</div>
                <div style="font-size: 11.5px; color: #64748B;">HTTP 206 Partial Content</div>
            </td>
            <td><span class="badge-tag badge-audio">${f.name.split('.').pop().toUpperCase()}</span></td>
            <td style="font-family: monospace;">${f.sizeText}</td>
            <td style="text-align: right;">
                <button class="btn-sm btn-green" onclick="playAudio('${f.url}', '${f.name}')">
                    ${isCurrent ? 'Đang Phát' : '▶ Phát'}
                </button>
                <a href="${f.url}" download="${f.name}" class="btn-sm btn-slate" style="text-decoration:none;">Tải Về</a>
            </td>
        `;
        tbody.appendChild(tr);
    });

    if (audioFiles.length > 0 && (!currentAudioUrl || !audioFiles.some(f => f.url === currentAudioUrl))) {
        playAudio(audioFiles[0].url, audioFiles[0].name, false);
    }
}

function playAudio(url, name, autoStart = true) {
    currentAudioUrl = url;
    renderAudioPlaylist();

    const player = document.getElementById('audioPlayer');
    const lbl = document.getElementById('lblAudioPlayingName');
    const btnTab = document.getElementById('btnAudioTab');
    const btnDl = document.getElementById('btnAudioDownload');

    if (lbl) lbl.textContent = name;
    if (player) {
        player.src = url;
        if (autoStart) {
            player.play().catch(() => {});
        }
    }
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name;
    }
}

// 5. Quan ly Video (Cinema Player & Grid)
function renderVideoGallery() {
    const videoFiles = mediaData.filter(f => {
        const n = f.name.toLowerCase();
        return n.endsWith('.mp4') || n.endsWith('.webm') || n.endsWith('.mkv');
    });

    const grid = document.getElementById('videoGalleryGrid');
    if (!grid) return;

    grid.innerHTML = '';
    if (videoFiles.length === 0) {
        grid.innerHTML = '<div class="empty-state">Khong co file video trong media/</div>';
        return;
    }

    videoFiles.forEach(v => {
        const card = document.createElement('div');
        const isCurrent = v.url === currentVideoUrl;
        card.className = 'video-card' + (isCurrent ? ' active' : '');
        card.onclick = () => playVideo(v.url, v.name);

        card.innerHTML = `
            <div class="video-card-thumb">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polygon points="5 3 19 12 5 21 5 3"></polygon>
                </svg>
            </div>
            <div class="video-card-body">
                <div class="video-card-title">${v.name}</div>
                <div class="video-card-meta">${v.sizeText} &bull; HTTP 206 Range</div>
            </div>
        `;
        grid.appendChild(card);
    });

    if (videoFiles.length > 0 && (!currentVideoUrl || !videoFiles.some(f => f.url === currentVideoUrl))) {
        playVideo(videoFiles[0].url, videoFiles[0].name);
    }
}

function playVideo(url, name) {
    currentVideoUrl = url;
    renderVideoGallery();

    const player = document.getElementById('videoPlayer');
    const lbl = document.getElementById('lblVideoPlayingName');
    const btnTab = document.getElementById('btnVideoTab');
    const btnDl = document.getElementById('btnVideoDownload');

    if (lbl) lbl.textContent = name;
    if (player) {
        player.src = url;
        player.load();
    }
    if (btnTab) btnTab.href = url;
    if (btnDl) {
        btnDl.href = url;
        btnDl.download = name;
    }
}

// 6. Xu ly Upload tep tin qua HTTP POST
function setupUploadHandlers() {
    const fileInput = document.getElementById('fileUploadInput');
    const dropZone = document.getElementById('uploadDropZone');

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
            }, 600);
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
