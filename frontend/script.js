// 상수 선언 (로컬스토리지 키 등)
const STORAGE_KEYS = {
  VIEW_COUNT: "viewCount",
  IS_LOGGED_IN: "isLoggedIn",
  CURRENT_USER: "currentUser",
  TOKEN: "token",
};

// 상태 관리
let viewCount = 0;
const MAX_FREE_VIEWS = 3;
let isLoggedIn = false;
let currentUser = null;

// 토큰이 유효하지 않은 경우 처리
function handleInvalidToken() {
  clearToken();
  isLoggedIn = false;
  currentUser = null;
  localStorage.removeItem(STORAGE_KEYS.IS_LOGGED_IN);
  localStorage.removeItem(STORAGE_KEYS.CURRENT_USER);
  updateAuthUI();
  alert("세션이 만료되었습니다. 다시 로그인해주세요.");
}

// 상태 저장 함수
function saveState() {
  localStorage.setItem(STORAGE_KEYS.VIEW_COUNT, viewCount);
  localStorage.setItem(STORAGE_KEYS.IS_LOGGED_IN, isLoggedIn);
  localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(currentUser));
}

// 상태 로드 함수
async function loadState() {
  viewCount = parseInt(localStorage.getItem(STORAGE_KEYS.VIEW_COUNT)) || 0;
  const token = getToken();
  if (token) {
    try {
      try {
        // 토큰 유효성 검사
        const response = await apiCall('/users/validate-token', 'GET');
        
        if (response && response.valid === true) {
          isLoggedIn = true;
          // 보안을 위해 필요한 정보만 저장
          currentUser = {
              userId: response.user?.userId,
              nickname: response.user?.nickname,
              name: response.user?.name
          };
          localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(currentUser));
          
          // 토큰 만료 시간이 1시간 이내일 경우 자동으로 토큰 갱신 시도
          const tokenExpiration = new Date(response.expiration);
          const now = new Date();
          const timeUntilExpiration = tokenExpiration.getTime() - now.getTime();
          
          // 토큰 만료 시간이 1시간 이내이거나, 서버 응답이 403 Forbidden인 경우 토큰 갱신 시도
          if (timeUntilExpiration <= 60 * 60 * 1000 || response.status === 403) {
            try {
              const refreshTokenResponse = await apiCall('/users/refresh-token', 'POST');
              if (refreshTokenResponse && refreshTokenResponse.token) {
                saveToken(refreshTokenResponse.token);
                console.log('Token refreshed successfully');
              }
            } catch (refreshError) {
              console.error('Token refresh failed:', refreshError);
              handleInvalidToken();
            }
          }
        } else {
          // 토큰이 유효하지 않은 경우
          console.warn('Token is invalid:', response);
          handleInvalidToken();
        }
      } catch (error) {
        console.error('Token validation failed:', error);
        handleInvalidToken();
      }
    } catch (error) {
      console.error('Token validation failed:', error);
      // 서버 통신 실패나 예기치 않은 에러 발생 시에도 토큰 유지
      // 서버가 다시 연결되면 자동으로 토큰 유효성 검사가 실행됨
      console.log('Maintaining token for next validation attempt');
      isLoggedIn = false;
      currentUser = null;
    }
  } else {
    isLoggedIn = false;
    currentUser = null;
  }
  updateAuthUI();
}

// API 설정
const API_BASE_URL = 'http://localhost:80/api'

// API 호출 함수 (에러 메시지 개선)
// ID 중복 확인 API 추가
async function checkIdDuplication(userId) {
  try {
    const response = await apiCall(`/users/check-id/${userId}`, "GET");
    return response.exists; // true/false 반환
  } catch (error) {
    throw error;
  }
}

async function apiCall(endpoint, method = "GET", data = null) {
  const token = getToken();
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      method,
      headers,
      body: data ? JSON.stringify(data) : null,
    });
    
    // Read the response body once as text
    const textResponse = await response.text();
    
    if (!response.ok) {
      let errorMsg = "API 호출 실패";
      try {
        // Try to parse the error response as JSON
        const errJson = JSON.parse(textResponse);
        if (typeof errJson === 'object') {
          // 필드별 에러가 있을 경우 각 필드별 메시지 표시
          if (Object.keys(errJson).length > 0) {
            errorMsg = "입력한 정보를 확인해주세요:\n\n";
            Object.entries(errJson).forEach(([field, message]) => {
              errorMsg += `${field} - ${message}\n`;
            });
          } else {
            errorMsg = errJson.message || errJson.error || errorMsg;
          }
        } else {
          errorMsg = errJson;
        }
      } catch {
        errorMsg = textResponse; // Use the raw text as error message
      }
      throw new Error(errorMsg);
    }
    
    // Try to parse the response as JSON
    try {
      return JSON.parse(textResponse);
    } catch {
      try {
        // Try to parse JSON string wrapped in quotes
        return JSON.parse(textResponse.replace(/^\"|\"$/g, ''));
      } catch {
        // If all else fails, return the raw text
        return { message: textResponse };
      }
    }
  } catch (error) {
    console.error("API Error:", error);
    throw error;
  }
}

// 토큰 관리 함수들
function saveToken(token) {
  localStorage.setItem(STORAGE_KEYS.TOKEN, token);
}

function getToken() {
  return localStorage.getItem(STORAGE_KEYS.TOKEN);
}

function clearToken() {
  localStorage.removeItem(STORAGE_KEYS.TOKEN);
}

// UI 업데이트 함수
function updateAuthUI() {
    const loginBtn = document.getElementById("login-btn-navbar");
    const profileBtn = document.getElementById("profile-btn");
    const logoutBtn = document.getElementById("logout-btn-navbar");

    if (loginBtn) {
      loginBtn.style.display = isLoggedIn ? "none" : " inline-block";
    }
    if (profileBtn) {
      profileBtn.style.display = isLoggedIn ?  "inline-block" : "none";
    }
    if (logoutBtn) {
      logoutBtn.style.display = isLoggedIn ? "inline-block" : "none";
    }
}

// 페이지 초기화
document.addEventListener("DOMContentLoaded", () => {
  // 페이지 로드 시 로그인 상태 체크
  loadState();


// 에러 처리 (best practice: error.message 우선, fallback)
function handleApiError(error) {
  if (error && error.message) { 
    try {
      // JSON 문자열일 경우 파싱
      const errorData = JSON.parse(error.message);
      if (typeof errorData === 'object') {
        // 여러 필드 에러가 있을 경우 모두 표시
        if (Object.keys(errorData).length > 0) {
          let errorMessage = "입력한 정보를 확인해주세요:\n\n";
          Object.entries(errorData).forEach(([field, message]) => {
            errorMessage += `${field} - ${message}\n`;
          });
          alert(errorMessage);
          return;
        }
      }
    } catch (e) {
      // 파싱 실패 시 기존 로직 유지
    }

    if (error.message === "news is undefined") {
      alert("뉴스 데이터를 불러오는 데 실패했습니다. 다시 시도해주세요.");
    } else if (error.message === "Invalid credentials") {
      alert("아이디 또는 비밀번호가 맞지 않습니다. 다시 시도해주세요.");
    } else {
      alert(error.message);
    }
    console.error("Network Error:", error);
  }
}
  const categoryButtons = document.querySelectorAll(".category-btn");
  const randomBtn = document.getElementById("random-btn");

  const logoutBtn = document.getElementById("logout-btn-navbar");


  // 로그아웃 기능 추가
  logoutBtn.addEventListener("click", () => {
    clearToken();
    isLoggedIn = false;
    currentUser = null;
    viewCount = 0;
    saveState();
    updateAuthUI();
    alert("로그아웃되었습니다.");
  });

  // 모달 닫기
  document.querySelectorAll(".close").forEach((closeBtn) => {
    closeBtn.addEventListener("click", () => {
      document.querySelector("#auth-modal").style.display = "none";
      document.querySelector("#profile-modal").style.display = "none";
    });
  });

  const newsContainer = document.querySelector(".news-container");

  let isLoading = false;
  let loadingSpinner = document.querySelector('.loading-spinner');
  if (!loadingSpinner) {
    loadingSpinner = document.createElement("div");
    loadingSpinner.className = "loading-spinner"; 
    newsContainer.appendChild(loadingSpinner);
    loadingSpinner.style.display = "none";
  }

  // Show the news-empty-message by default
const newsEmptyMessage = document.getElementById("news-empty-message");

// 로딩 중 주요 버튼 disabled 처리 함수
  function setLoadingStateForButtons(disabled) {
    [
      document.getElementById("login-btn"),
      document.getElementById("signup-btn"),
      document.getElementById("update-profile-btn"),
      document.getElementById("change-password-btn"),
      document.getElementById("delete-account-btn"),
      document.getElementById("random-btn"),
    ].forEach((btn) => {
      if (btn) btn.disabled = !!disabled;
    });
  }

  // Show loading state
  function showLoading() {
    if (isLoading) return; // 이미 로딩 중이면 중복 호출 방지
    isLoading = true;
    loadingSpinner.style.display = "block";
    newsContainer.style.opacity = "0.5";
    setLoadingStateForButtons(true);
  }

  // Hide loading state
  function hideLoading() {
    isLoading = false;
    loadingSpinner.style.display = "none";
    newsContainer.style.opacity = "1";
    setLoadingStateForButtons(false);
  }

  

  // 뉴스 가져오기 함수 (오늘의 랜덤 뉴스)
  async function fetchTodayRandomNews(category = "전체") {
  try {
    const apiResponse = await apiCall(
      `/news/random/today?category=${category}`,
      "GET"
    );

    // Hide the news-empty-message if news is successfully fetched
    newsEmptyMessage.style.display = "none";

    // 1. API 응답이 배열이고, 첫 번째 요소가 유효한 객체인 경우
    if (Array.isArray(apiResponse) && apiResponse.length > 0 && typeof apiResponse[0] === 'object' && apiResponse[0] !== null) {
      return apiResponse[0]; // 배열의 첫 번째 뉴스를 반환
    }
    // 2. API 응답 객체 내에 'news' 속성으로 뉴스 객체가 있는 경우
    else if (apiResponse && typeof apiResponse.news === 'object' && apiResponse.news !== null) {
      return apiResponse.news;
    }
    // 3. API 응답 자체가 뉴스 객체인 경우
    else if (apiResponse && typeof apiResponse === 'object' && !Array.isArray(apiResponse) && apiResponse.title) {
      return apiResponse;
    }
    // 4. 위 조건들에 해당하지 않으면, 뉴스 데이터를 찾을 수 없는 것으로 간주
    else {
      console.warn("오늘의 랜덤 뉴스 데이터 형식이 예상과 다릅니다. API 응답:", apiResponse);
      throw new Error("news is undefined");
    }
  } catch (error) {
    throw error;
  }
}
  

  // 뉴스 검색 함수
  async function searchNews(date, category) {
  const [month, day] = date.split('-');
  try {
    const apiResponse = await apiCall(
      `/news/search?month=${month}&day=${day}&category=${category}`,
      "GET"
    );

    // Hide the news-empty-message if news is successfully fetched
    newsEmptyMessage.style.display = "none";

    // 1. API 응답이 배열이고, 첫 번째 요소가 유효한 객체인 경우
    if (Array.isArray(apiResponse) && apiResponse.length > 0 && typeof apiResponse[0] === 'object' && apiResponse[0] !== null) {
      return apiResponse[0]; // 검색 결과 중 첫 번째 뉴스를 반환
    }
    // 2. API 응답 객체 내에 'news' 속성으로 뉴스 객체가 있는 경우
    else if (apiResponse && typeof apiResponse.news === 'object' && apiResponse.news !== null) {
      return apiResponse.news;
    }
    // 3. API 응답 자체가 뉴스 객체인 경우
    else if (apiResponse && typeof apiResponse === 'object' && !Array.isArray(apiResponse) && apiResponse.title) {
      return apiResponse;
    }
    // 4. 위 조건들에 해당하지 않으면, 뉴스 데이터를 찾을 수 없는 것으로 간주
    else {
      console.warn("검색된 뉴스 데이터 형식이 예상과 다릅니다. API 응답:", apiResponse);
      throw new Error("news is undefined");
    }
  } catch (error) {
    throw error;
  }
}
  

  // 카테고리 버튼 클릭 시
  categoryButtons.forEach((button) => {
    button.addEventListener("click", async () => {
      if (isLoading) return;
      const category = button.dataset.category;
      categoryButtons.forEach((btn) => btn.classList.remove("active"));
      button.classList.add("active");
      // 날짜 선택 여부 확인
      const monthSelect = document.getElementById('month');
      const daySelect = document.getElementById('day');
      const selectedMonth = monthSelect.value;
      const selectedDay = daySelect.value;
      
      if (viewCount >= MAX_FREE_VIEWS && !isLoggedIn) {
        alert("무료 조회 횟수를 초과했습니다. 로그인 후 계속 사용해주세요.");
        showLoginModal();
        return;
      }
      
      showLoading();
      try {
        let news;
        if (selectedMonth && selectedDay) {
          // 날짜가 선택되어 있다면 searchNews 호출
          const selectedDate = `${selectedMonth}-${selectedDay}`;
          news = await searchNews(selectedDate, category);
        } else {
          // 날짜가 선택되지 않았다면 fetchTodayRandomNews 호출
          news = await fetchTodayRandomNews(category);
        }
        displayNews(news);
        updateViewCount();
      } catch (error) {
        handleApiError(error);
      } finally {
        hideLoading();
      }
    });
  });


  const searchBtn = document.getElementById("search-date-btn");
  searchBtn.addEventListener("click", async () => {
    const month = document.getElementById('month').value;
    const day = document.getElementById('day').value;
    const formattedMonth = month.padStart(2, '0');
    const formattedDay = day.padStart(2, '0');

    const date = `${formattedMonth}-${formattedDay}`;

    const category = document.querySelector(".category-btn.active").dataset.category;

    if (!month || !day) { // 월 또는 일이 입력되지 않았을 경우
      alert("월과 일을 모두 선택해주세요.");
      return;
    }
    showLoading();
    try {
      const news = await searchNews(date, category);
      displayNews(news);
      updateViewCount();
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });


    // 오늘의 뉴스 불러오기
  async function loadTodayNews(category) {
    if (isLoading) return;
    if (viewCount >= MAX_FREE_VIEWS && !isLoggedIn) {
      alert("무료 조회 횟수를 초과했습니다. 로그인 후 계속 사용해주세요.");
      showLoginModal();
      return;
    }
    showLoading();
    try {
      const news = await fetchTodayRandomNews(category);
      if (!news) {
        throw new Error("news is undefined");
      }
      displayNews(news);
      updateViewCount();
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  }
  
  //오늘의 뉴스 버튼 클릭 시
  randomBtn.addEventListener("click", () => {  
    const activeCategory = document.querySelector(".category-btn.active");
    const category = activeCategory ? activeCategory.dataset.category : "all";
    loadTodayNews(category);
  });

  // 인증 모달 관련 이벤트
  const authTabBtns = document.querySelectorAll(".auth-tab-btn");
  const authForms = document.querySelectorAll(".auth-form");
  const authModal = document.getElementById("auth-modal");
  const profileModal = document.getElementById("profile-modal");
  // 탭 전환
  authTabBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      // 모든 탭 버튼에서 active 클래스 제거
      authTabBtns.forEach((b) => b.classList.remove("active"));
      // 클릭한 탭 버튼에 active 클래스 추가
      btn.classList.add("active");

      // 모든 폼에서 active 클래스 제거
      authForms.forEach((form) => {
        form.classList.remove("active");
        form.style.display = "none"; // 폼 숨기기
      });

      // 클릭한 탭에 해당하는 폼만 활성화
      const targetForm = document.getElementById(`${btn.dataset.tab}-form`);
      if (targetForm) {
        targetForm.classList.add("active");
        targetForm.style.display = "block"; // 폼 보이기
      }
    });
  });

  // 네비게이션 바의 로그인 버튼 클릭 시 모달 열기
  document.getElementById("login-btn-navbar").addEventListener("click", () => {  
    showLoginModal();
  });

  // 모달 내부 로그인 버튼 클릭 시 로그인 처리
  document.getElementById("login-btn").addEventListener("click", async () => {
    if (isLoading) return;
    const userId = document.getElementById("login-id").value;
    const password = document.getElementById("login-password").value;

    if (!userId || !password) {
      alert("아이디와 비밀번호를 입력해주세요.");
      return;
    }
    showLoading();
    try {
      const response = await apiCall("/users/login", "POST", {
        userId,
        password,
      });

      if (response.accessToken) {
        saveToken(response.accessToken);
        isLoggedIn = true;
        currentUser = { userId };
        saveState();
        authModal.style.display = "none";
        updateAuthUI();
        alert("로그인되었습니다.");
      } else if (response.error) {
        alert(response.error);
      } else {
        alert(response.message || "로그인에 실패했습니다.");
      }
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });

  // 아이디 중복확인 버튼 이벤트 리스너
  document.getElementById("check-id-btn").addEventListener("click", async () => {
    const userId = document.getElementById("signup-id").value.trim();
    if (!userId) {
      alert("아이디를 입력해주세요.");
      return;
    }

    try {
      showLoading();
      const isIdExists = await checkIdDuplication(userId);
      const messageDiv = document.getElementById("id-check-message");
      if (isIdExists === false) {
        messageDiv.textContent = "사용 가능한 아이디입니다.";
        messageDiv.style.color = "green";
      } else if (isIdExists === true) { 
        messageDiv.textContent = "이미 사용 중인 아이디입니다.";
        messageDiv.style.color = "red";
      } else {
        messageDiv.textContent = "아이디 확인에 실패했습니다. 다시 시도해주세요.";
        messageDiv.style.color = "orange";
      }
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });

  // 회원가입 처리
  document.getElementById("signup-btn").addEventListener("click", async () => {
    console.log("Signup button clicked"); // Debugging log
    if (isLoading) return;

    const requiredFields = [
      "signup-id",
      "signup-password",
      "signup-confirm",
      "signup-email",
      "signup-nickname",
      "signup-phone",
      "signup-name",
      "signup-birthdate",
      "signup-gender",
    ];

    const missingFields = requiredFields.filter((id) => !document.getElementById(id));
    if (missingFields.length > 0) {
      console.error("Missing elements in DOM:", missingFields); // Debugging log
      alert("필수 입력 필드가 누락되었습니다. 관리자에게 문의하세요.");
      return;
    }

    const userId = document.getElementById("signup-id").value;
    const password = document.getElementById("signup-password").value;
    const confirmPassword = document.getElementById("signup-confirm").value;
    const email = document.getElementById("signup-email").value;
    const nickname = document.getElementById("signup-nickname").value;
    const phone = document.getElementById("signup-phone").value;
    const name = document.getElementById("signup-name").value;
    const birthDate = document.getElementById("signup-birthdate").value;
    const gender = document.getElementById("signup-gender").value;

    if (!userId || !password || !confirmPassword || !email || !nickname || !phone || !name || !birthDate || !gender) {
      alert("모든 필드를 입력해주세요.");
      return;
    }

    if (password !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    showLoading();
    try {
      const response = await apiCall("/users/register", "POST", {
        userId,
        password,
        confirmPassword,
        email,
        nickname,
        phone,
        name,
        birthDate,
        gender,
      });

      if (response.message) {
        alert(response.message);
      } else if (response.error) {
        // 에러 객체가 있을 경우 각 필드별 에러 메시지 표시
        if (typeof response.error === 'object') {
          let errorMessage = "입력한 정보를 확인해주세요:\n\n";
          Object.entries(response.error).forEach(([field, message]) => {
            errorMessage += `${field} - ${message}\n`;
          });
          alert(errorMessage);
        } else {
          alert(response.error);
        }
      } else {
        alert("회원가입이 완료되었습니다.");
      }

      // 입력 필드 초기화
      requiredFields.forEach((id) => {
        const element = document.getElementById(id);
        if (element) {
          element.value = "";
        }
      });

      authModal.style.display = "none";
      document.querySelector(".auth-tab-btn[data-tab='login']").click();
    } catch (error) {
      console.error("Signup error", error); // Debugging log
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });

  const profileBtn = document.getElementById("profile-btn");
  // 모달 열릴 때 첫 input 포커스(접근성)
  function focusFirstInput(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    const firstInput = modal.querySelector("input, select, button");
    if (firstInput) firstInput.focus();
  }

  // 인증/프로필 모달 열기 시 포커스 이동
  profileBtn.addEventListener("click", async () => {
    if (!isLoggedIn) {
      authModal.style.display = "block";
      return;
    }
    profileModal.style.display = "block";
    await loadUserProfile();
    focusFirstInput("profile-modal");
  });

  // 프로필 정보 불러오기
  async function loadUserProfile() {
    if (!isLoggedIn) return;

    try {
      const response = await apiCall("/users/me", "GET");
      currentUser = {
        userId: response.userId,
        nickname: response.nickname,
        name: response.name
      };
      saveState();
      // 한글 인코딩 문제 해결을 위한 함수
      function decodeText(text) {
        if (!text) return "";
        try {
          return decodeURIComponent(escape(text));
        } catch (e) {
          return text;
        }
      }

      // 읽기 전용 필드
      document.getElementById("profile-id").textContent = response.userId;
      document.getElementById("profile-name").textContent = decodeText(response.name) || "";
      document.getElementById("profile-nickname").textContent = decodeText(response.nickname) || "";
      document.getElementById("profile-email").textContent = response.email || "";
      document.getElementById("profile-phone").textContent = response.phone || "";
      document.getElementById("profile-birthdate").textContent = response.birthDate || "";
      document.getElementById("profile-gender").textContent = response.gender || "";

    } catch (error) {
        console.error('프로필 정보 로드 실패:', error);
        handleApiError(error);
    }
  }

  // 회원 탈퇴
  document.getElementById("delete-account-btn").addEventListener("click", async () => {
    if (isLoading) return;

    if (!confirm("정말로 회원탈퇴를 진행하시겠습니까?\n탈퇴 후에는 모든 데이터가 삭제되며, 복구가 불가능합니다.")) {
      return;
    }

    try {
      showLoading();
      await apiCall('/users/withdraw', 'DELETE');
      clearToken();
      isLoggedIn = false;
      currentUser = null;
      saveState();
      updateAuthUI();
      alert("회원탈퇴가 완료되었습니다.");
      const profileModal = document.getElementById("profile-modal");
      profileModal.style.display = "none";
    } catch (error) {
      console.error("회원탈퇴 실패:", error);
      alert("회원탈퇴 중 오류가 발생했습니다. 다시 시도해주세요.");
    } finally {
      hideLoading();
    }
  });

  // 프로필 수정
  document.getElementById("update-profile-btn").addEventListener("click", async () => {
    if (isLoading) return;
    
    const email = document.getElementById("profile-email").value;
    const nickname = document.getElementById("profile-nickname").value;
    const phone = document.getElementById("profile-phone").value;
    const name = document.getElementById("profile-name").value;
    const birthDate = document.getElementById("profile-birthdate").value;
    const gender = document.getElementById("profile-gender").value;

    // 폼 유효성 검사
    const emailInput = document.getElementById("profile-email");
    const nicknameInput = document.getElementById("profile-nickname");
    const phoneInput = document.getElementById("profile-phone");
    const nameInput = document.getElementById("profile-name");
    const birthdateInput = document.getElementById("profile-birthdate");
    const genderSelect = document.getElementById("profile-gender");

    if (!emailInput.checkValidity()) {
      alert(emailInput.validationMessage);
      return;
    }
    if (!nicknameInput.checkValidity()) {
      alert(nicknameInput.validationMessage);
      return;
    }
    if (!phoneInput.checkValidity()) {
      alert(phoneInput.validationMessage);
      return;
    }
    if (!nameInput.checkValidity()) {
      alert(nameInput.validationMessage);
      return;
    }
    if (!birthdateInput.checkValidity()) {
      alert("생년월일을 선택해주세요.");
      return;
    }
    if (gender === "") {
      alert("성별을 선택해주세요.");
      return;
    }
    showLoading();
    try {
      const response = await apiCall("/users/me", "PATCH", {
        email,
        nickname,
        phone,
        name,
        birthDate,
        gender,
      });

      currentUser = response;
      saveState();
      updateAuthUI();
      profileModal.style.display = "none";
      alert("프로필이 수정되었습니다.");
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });

  // 비밀번호 변경 버튼 클릭 이벤트(폼 show/hide만 담당, 중복 제거)
  document.getElementById("change-password-btn").addEventListener("click", () => {
    const passwordForm = document.getElementById("change-password-form");
    const changePasswordBtn = document.getElementById("change-password-btn");
    passwordForm.style.display = passwordForm.style.display === "none" ? "block" : "none";
    changePasswordBtn.classList.toggle("active");
  });

  // 비밀번호 변경 (폼 submit으로 변경, 중복 리스너 제거)
  document.getElementById("change-password-form").addEventListener("submit", async (e) => {
    e.preventDefault();
    if (isLoading) return;
    const currentPassword = document.getElementById("current-password").value;
    const newPassword = document.getElementById("new-password").value;
    const confirmPassword = document.getElementById("confirm-new-password").value;

    // 비밀번호 폼 유효성 검사
    const currentPasswordInput = document.getElementById("current-password");
    const newPasswordInput = document.getElementById("new-password");
    const confirmPasswordInput = document.getElementById("confirm-new-password");

    if (!currentPasswordInput.checkValidity()) {
      alert(currentPasswordInput.validationMessage);
      return;
    }
    if (!newPasswordInput.checkValidity()) {
      alert(newPasswordInput.validationMessage);
      return;
    }
    if (!confirmPasswordInput.checkValidity()) {
      alert(confirmPasswordInput.validationMessage);
      return;
    }

    if (newPassword !== confirmPassword) {
      alert("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    showLoading();
    try {
      await apiCall("/users/password", "POST", {
        currentPassword,
        newPassword,
      });
      alert("비밀번호가 변경되었습니다.");
      document.getElementById("change-password-form").reset();
      document.getElementById("change-password-form").style.display = "none";
      document.getElementById("change-password-btn").classList.remove("active");
    } catch (error) {
      handleApiError(error);
    } finally {
      hideLoading();
    }
  });

  // 회원 탈퇴 버튼 클릭 이벤트
  document.getElementById("delete-account-btn").addEventListener("click", async () => {
    try {
      await apiCall("/users/withdraw", "DELETE");
      clearToken();
      isLoggedIn = false;
      currentUser = null;
      viewCount = 0;
      saveState();
      updateAuthUI();
      profileModal.style.display = "none";
      alert("회원 탈퇴가 완료되었습니다.");
    } catch (error) {
      handleApiError(error);
    }
  });

  // 인증 상태에 따른 UI 업데이트
  function updateAuthUI() {
    const loginBtn = document.getElementById("login-btn-navbar");
    const profileBtn = document.getElementById("profile-btn");
    const logoutBtn = document.getElementById("logout-btn-navbar");

    if (loginBtn) {
      loginBtn.style.display = isLoggedIn ? "none" : " inline-block";
    }
    if (profileBtn) {
      profileBtn.style.display = isLoggedIn ?  "inline-block" : "none";
    }
    if (logoutBtn) {
      logoutBtn.style.display = isLoggedIn ? "inline-block" : "none";
    }
  }

  // 년도 차이 계산 함수
  function calculateYearsAgo(dateString) {
    const today = new Date();
    const newsDate = new Date(dateString);
    const yearsAgo = today.getFullYear() - newsDate.getFullYear();
    return yearsAgo;
  }

  // 뉴스 표시 함수
  function displayNews(news) {
    if (!news) {
      console.error("Invalid news data received:", news);
      throw new Error("news is undefined");
    }
    document.getElementById("news-title").textContent = news.title;
    const yearsAgo = calculateYearsAgo(news.publishedDate);
    const formattedDate = news.publishedDate.replace(/-/g, '.');
    const today = new Date();
    const publishedDate = new Date(news.publishedDate);
    if (publishedDate.getDate() === today.getDate() && publishedDate.getMonth() === today.getMonth()) {
      document.getElementById("published-date").textContent = `발행일: ${yearsAgo}년 전 오늘 (${formattedDate})`;
    } else {
      document.getElementById("published-date").textContent = `발행일: ${yearsAgo}년 전 당시 (${formattedDate})`;
    }
    
    document.getElementById("main-category").textContent = `대분류: ${news.mainCategory}`;
    document.getElementById(
      "category-levels"
    ).textContent = `카테고리: ${news.categoryLevel1}, ${news.categoryLevel2}, ${news.categoryLevel3}`;
    // Combine content and address link for inline display
    const contentWithLink = `${news.content} <a id="address" href="${news.address}" target="_blank" rel="noopener noreferrer">자세히 보기</a>`;
    document.getElementById("news-content").innerHTML = contentWithLink;
    document.getElementById("press").textContent = `출처: ${news.press}`;
    document.getElementById("reporter").textContent = `기자: ${news.reporter}`;
  }

  // 로그인 모달 표시 함수
  function showLoginModal() {
    const loginModal = document.getElementById("auth-modal");
    const loginForm = document.getElementById("login-form");
    const signupForm = document.getElementById("signup-form");

    // 로그인 폼 활성화, 회원가입 폼 비활성화
    loginForm.classList.add("active");
    loginForm.style.display = "block";
    signupForm.classList.remove("active");
    signupForm.style.display = "none";

    loginModal.style.display = "block";
    focusFirstInput("auth-modal");
  }

  // 모달 외부 클릭 시 닫기
  const loginModal = document.getElementById("auth-modal");
  window.addEventListener("click", (event) => {
    if (event.target === loginModal || event.target === profileModal) {
      event.target.style.display = "none";
    }
  });

  // 프로필 모달 닫기 버튼 클릭 이벤트
  document.getElementById("profile-close").addEventListener("click", () => {
    profileModal.style.display = "none";
    const profileForm = document.getElementById("profile-form");
    if (profileForm.classList.contains("editing")) {
      profileForm.classList.remove("editing");
    }
    const passwordForm = document.getElementById("change-password-form");
    if (passwordForm.style.display !== "none") {
      passwordForm.style.display = "none";
      document.getElementById("change-password-btn").classList.remove("active");
    }
  });

  // 뷰 카운트 업데이트 함수
  function updateViewCount() {
    viewCount++;
    saveState();
    if (viewCount >= MAX_FREE_VIEWS && !isLoggedIn) {
      alert("무료 조회 횟수를 초과했습니다. 로그인 후 계속 사용해주세요.");
      showLoginModal();
    }
  }



  // 페이지 진입 시 오늘의 뉴스만 자동 로드
  (async () => {
    loadState();
    if (viewCount < MAX_FREE_VIEWS || isLoggedIn) {
      showLoading();
      try {
        await loadTodayNews("all");
      } catch (error) {
        handleApiError(error);
      } finally {
        hideLoading();
      }
    } else {
      alert("무료 조회 횟수를 초과했습니다. 로그인 후 계속 사용해주세요.");
      showLoginModal();
    }
  })();

  // randomBtn.addEventListener("click", async () => {
  //   try {
  //     showLoading();
  //     const news = await fetchTodayRandomNews();
  //     if (news) {
  //       // Populate the news content here
  //       document.getElementById("news-title").textContent = news.title;
  //       document.getElementById("published-date").textContent = news.date;
  //       document.getElementById("main-category").textContent = news.category;
  //       document.getElementById("news-content").textContent = news.content;
  //       document.getElementById("press").textContent = news.press;
  //       document.getElementById("reporter").textContent = news.reporter;
  //     }
  //   } catch (error) {
  //     console.error("Failed to fetch news:", error);
  //     alert("뉴스를 가져오는 데 실패했습니다.");
  //   } finally {
  //     hideLoading();
  //   }
  // });
}
);