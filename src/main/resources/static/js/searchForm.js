document.addEventListener("DOMContentLoaded", function () {
    // activate tab buttons
    var tabs = document.querySelectorAll('.tab-button');
    var contents = document.querySelectorAll('.tab-content');

    tabs.forEach(function(tab) {
        tab.addEventListener('click', function() {
            tabs.forEach(function(t) { t.classList.remove('active'); });
            contents.forEach(function(c) { c.classList.remove('active'); });

            tab.classList.add('active');
            document.getElementById(tab.getAttribute('data-tab')).classList.add('active');
        });
    });
    tabs[1].click();

    // search tool checkboxes
    const allCheckbox = document.getElementById('favoriteGenre0');
    const genreCheckboxes = document.querySelectorAll('input[name="favoriteGenre"]:not(#favoriteGenre0)');

    allCheckbox.addEventListener('change', function () {
        if (this.checked) {
            genreCheckboxes.forEach(cb => cb.checked = false);
        } else {
            this.checked = true;
        }
    });
    genreCheckboxes.forEach(cb => {
        cb.addEventListener('change', function () {
            if (this.checked) {
                allCheckbox.checked = false;
            } else if (!Array.from(genreCheckboxes).some(cb => cb.checked)) {
                allCheckbox.checked = true;
            }
        });
    });
});

document.getElementById("toggleSearchTools").addEventListener("click", function() {
    const detailSearchBox = document.querySelector(".detail-search-box");
    if (detailSearchBox.style.display === "none" || detailSearchBox.style.display === "") {
        detailSearchBox.style.display = "block";
    } else {
        detailSearchBox.style.display = "none";
    }
});


// Search history (Local Storage)
document.addEventListener('DOMContentLoaded', () => {
    saveHistory = getSaveHistoryStatus();
    document.getElementById('saveStatus').textContent = saveHistory ? 'ON' : 'OFF';
    updateSearchHistory();
});

let saveHistory = true;
const getSaveHistoryStatus = () => {
    const status = localStorage.getItem('saveHistory');
    return status ? JSON.parse(status) : true; // 기본값은 true
};
const saveHistoryStatusToLocal = (status) => {
    localStorage.setItem('saveHistory', JSON.stringify(status));
};
const toggleSave = () => {
    saveHistory = !saveHistory;
    saveHistoryStatusToLocal(saveHistory);
    document.getElementById('saveStatus').textContent = saveHistory ? 'ON' : 'OFF';
    updateSearchHistory(); // 저장 옵션 변경 시 검색 히스토리 업데이트
};

const getFromLocal = () => {
    const history = localStorage.getItem('currentSearch');
    return history ? JSON.parse(history) : [];
};
const saveToLocal = (history) => {
    localStorage.setItem('currentSearch', JSON.stringify(history));
};
const handleSearch = () => {
    const query = document.getElementById('keyword').value.trim();
    if (query) {
        let history = getFromLocal();
        if (saveHistory) {
            const existingIndex = history.findIndex(item => item.keyword === query);

            if (existingIndex !== -1) {
                history[existingIndex].date = new Date();
                const updatedItem = history.splice(existingIndex, 1)[0];
                history.push(updatedItem);
            } else {
                history.push({ keyword: query, date: new Date() });
                if (history.length > 15) history.shift();
            }
            saveToLocal(history);
        }

        document.getElementById('keyword').value = '';
        updateSearchHistory();
        searchNovels();
    }
};

function searchNovels() {
    console.log("아니왜안돼");
    // 폼의 데이터를 가져옵니다.
    const keyword = document.getElementById("keyword").value;
    const isFree = document.getElementById("isFree").checked ? '1' : '0';  // 예시로 체크박스를 고려
    const isAdult = document.getElementById("isAdult").checked ? '1' : '0';
    const isCompleted = document.getElementById("isCompleted").checked ? '1' : '0';
    const minEp = document.getElementById("minEp").value;
    const maxEp = document.getElementById("maxEp").value;
    const orderBy = document.getElementById("orderBy").value;  // 예시로 선택값
    const favoriteGenres = getFavoriteGenres();  // 좋아하는 장르를 리스트로 가져오는 함수
    const excludeWordText = document.getElementById("excludeWordText").value;
    const excludeWordOptions = getExcludeWords();  // 제외 단어 리스트를 가져오는 함수

    // URL을 만듭니다.
    let url = `/search/novel?keyword=${encodeURIComponent(keyword)}&isFree=${isFree}&isAdult=${isAdult}&isCompleted=${isCompleted}&minEp=${minEp}&maxEp=${maxEp}&orderBy=${orderBy}&excludeWordText=${encodeURIComponent(excludeWordText)}`;

    // favoriteGenres와 excludeWordOptions는 리스트이므로 각 항목을 쿼리 파라미터로 변환합니다.
    if (favoriteGenres.length > 0) {
        url += `&favoriteGenre=${favoriteGenres.map(encodeURIComponent).join('&favoriteGenre=')}`;
    }
    if (excludeWordOptions.length > 0) {
        url += `&excludeWord=${excludeWordOptions.map(encodeURIComponent).join('&excludeWord=')}`;
    }

    // GET 요청을 보냅니다.
    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => response.text())  // 서버 응답을 텍스트로 받기
        .then(data => {
            // 서버에서 반환된 데이터를 처리
            console.log(data);  // 예시: 서버에서 반환된 HTML 또는 JSON을 콘솔에 출력
        })
        .catch(error => {
            console.error('Error:', error);  // 에러 처리
        });
}

// 예시: 좋아하는 장르를 가져오는 함수
function getFavoriteGenres() {
    const genreCheckboxes = document.querySelectorAll('input[name="favoriteGenre"]:checked');
    const selectedGenres = [];
    genreCheckboxes.forEach((checkbox) => {
        if (checkbox.value !== "") {  // "All"은 제외 (빈 값)
            selectedGenres.push(checkbox.value);
        }
    });
    return selectedGenres;
}

// 예시: 제외 단어 리스트를 가져오는 함수
function getExcludeWords() {
    const excludeWordCheckboxes = document.querySelectorAll('input[name="excludeWord"]:checked');
    const excludeWords = [];
    excludeWordCheckboxes.forEach((checkbox) => {
        excludeWords.push(checkbox.value);
    });
    return excludeWords;
}


const updateSearchHistory = () => {
    const searchHistoryContainer = document.getElementById('searchHistory');
    let searchHistory = JSON.parse(localStorage.getItem('currentSearch') || '[]');

    if (!saveHistory) {
        searchHistoryContainer.innerHTML = "<p>Search history saving is turned off.</p>";
        return;
    }

    if (searchHistory.length === 0) {
        searchHistoryContainer.innerHTML = "<p>No recent search history.</p>";
        return;
    }

    searchHistory = searchHistory.sort((a, b) => new Date(b.date) - new Date(a.date));

    searchHistoryContainer.innerHTML = searchHistory.map((item, index) => `
        <div class="search-history-item">
            <span class="search-term">${item.keyword}</span>
            <span class="search-date">${formatDate(new Date(item.date))}</span>
            <button class="delete-button" onclick="handleDeleteHistory(${searchHistory.length - 1 - index})">x</button>
        </div>
    `).join('');
};
const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const currentYear = new Date().getFullYear();

    return (year === currentYear) ? `${month}/${day}` : `${year}/${month}/${day}`;
};

const deleteAllHistory = () => {
    if (confirm("Are you sure you want to delete all search history?")) {
        localStorage.removeItem('currentSearch');
        updateSearchHistory();
    }
};
const handleDeleteHistory = (index) => {
    const searchHistory = JSON.parse(localStorage.getItem('currentSearch') || '[]');
    searchHistory.splice(index, 1); // 해당 인덱스의 검색어 삭제
    localStorage.setItem('currentSearch', JSON.stringify(searchHistory));
    updateSearchHistory(); // 삭제 후 검색 히스토리 업데이트
};

function checkEnter(event) {
    if (event.key === "Enter") { // Enter 키인지 확인
        handleSearch(); // handleSearch 함수 호출
    }
}

$(document).ready(function () {
    const searchInput = $('#keyword');
    const suggestionDropdown = $('#suggestionDropdown');
    const titleSuggestions = $('#titleSuggestions ul');
    const authorSuggestions = $('#authorSuggestions ul');

    searchInput.on('input', function () {
        const keyword = $(this).val();
        if (keyword.length > 0) {
            $.ajax({
                url: '/search/suggestions',
                method: 'POST',
                data: { keyword: keyword },
                success: function (response) {
                    // 제목 추천
                    titleSuggestions.empty();
                    response.titles.forEach(novel => {
                        titleSuggestions.append(`<li><a href="/novel/detail/${novel.nv_id}/episodes" target="_blank">${novel.nv_title}</a></li>`);
                    });

                    // 작가 추천
                    authorSuggestions.empty();
                    response.authors.forEach(author => {
                        authorSuggestions.append(`<li><a href="/profile/${author.userno}" target="_blank">${author.nickname}</a></li>`);
                    });

                    suggestionDropdown.show();
                },
                error: function () {
                    console.error('Failed to fetch suggestions');
                }
            });
        } else {
            suggestionDropdown.hide();
        }
    });

    // 클릭 이벤트 처리
    $(document).on('click', '#suggestionDropdown li', function () {
        searchInput.val($(this).text());
        suggestionDropdown.hide();
    });

    // Input 외부 클릭 시 드롭다운 숨김
    $(document).on('click', function (e) {
        if (!$(e.target).closest('.autocomplete-container').length) {
            suggestionDropdown.hide();
        }
    });
});
