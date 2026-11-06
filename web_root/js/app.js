// JavaScript handler for Form POST submit
document.addEventListener('DOMContentLoaded', () => {
    const postForm = document.getElementById('postForm');
    const postResult = document.getElementById('postResult');

    if (postForm) {
        postForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const formData = new FormData(postForm);
            const params = new URLSearchParams();
            for (const [key, value] of formData.entries()) {
                params.append(key, value);
            }

            try {
                postResult.style.display = 'block';
                postResult.textContent = '⏳ Đang gửi dữ liệu qua HTTP POST...';

                const response = await fetch('/api/submit', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: params.toString()
                });

                const responseData = await response.text();
                postResult.textContent = `✅ Phản hồi từ HTTP Server (Status ${response.status}):\n${responseData}`;
            } catch (err) {
                postResult.textContent = `❌ Lỗi khi gửi POST request: ${err.message}`;
            }
        });
    }
});
