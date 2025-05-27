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
    if (!response.ok) {
      let errorMsg = "API 호출 실패";
      try {
        const errJson = await response.json();
        errorMsg = errJson.message || errJson.error || errorMsg;
      } catch {}
      throw new Error(errorMsg);
    }
    return await response.json();
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

document.addEventListener("DOMContentLoaded", () => {
  function saveState() {
    localStorage.setItem(STORAGE_KEYS.VIEW_COUNT, viewCount);
    localStorage.setItem(STORAGE_KEYS.IS_LOGGED_IN, isLoggedIn);
    localStorage.setItem(STORAGE_KEYS.CURRENT_USER, JSON.stringify(currentUser));
  }

  function loadState() {
    viewCount = parseInt(localStorage.getItem(STORAGE_KEYS.VIEW_COUNT)) || 0;
    isLoggedIn = localStorage.getItem(STORAGE_KEYS.IS_LOGGED_IN) === "true";
    currentUser = JSON.parse(localStorage.getItem(STORAGE_KEYS.CURRENT_USER) || "null");
    updateAuthUI();
  }

  // 에러 처리 (best practice: error.message 우선, fallback)
  function handleApiError(error) {
    if (error && error.message) { 
      if (error.message === "news is undefined") {
        alert("뉴스 데이터를 불러오는 데 실패했습니다. 다시 시도해주세요.");
      } else {
        alert(error.message);
      }
      console.error("API Error:", error);
    } else {
      alert("현재 뉴스를 불러오는데 문제가 있습니다. 잠시 후 다시 시도해 주세요.");
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
      const response = await apiCall(
        `/news/random/today?category=${category}`,
        "GET"
      );
      return response.news;
    } catch (error) {
      throw error;
    }
  }

  // 뉴스 검색 함수
  async function searchNews(date, category) {
    const [year, month, day] = date.split('-');
    try {
      const response = await apiCall(
        `/news/search?year=${year}&month=${month}&day=${day}&category=${category}`,
        "GET"
      );
      return response.news;
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
      if (viewCount >= MAX_FREE_VIEWS && !isLoggedIn) {
        alert("무료 조회 횟수를 초과했습니다. 로그인 후 계속 사용해주세요.");
        showLoginModal();
        return;
      }
      showLoading();
      try {
        const news = await fetchTodayRandomNews(category);
        displayNews(news);
        updateViewCount();
      } catch (error) {
        handleApiError(error);
      } finally {
        hideLoading();
      }
    });
  });

  // 날짜 선택 후 뉴스 검색
  const dateInput = document.getElementById("news-date");
  dateInput.addEventListener("change", () => {
    const date = dateInput.value;
    const category = document.querySelector(".category-btn.active").dataset.category;
    if (!date) {
      alert("날짜를 선택해주세요.");
      return;
    }
    searchNews(date, category);
  }
  );

  const searchBtn = document.getElementById("search-date-btn");
  searchBtn.addEventListener("click", async () => {
    const date = document.getElementById("news-date").value;
    const category = document.querySelector(".category-btn.active").dataset.category;
    if (!date) {
      alert("날짜를 선택해주세요.");
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
      currentUser = response;
      saveState();

      // 프로필 정보 표시
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

      // 입력 필드
      document.getElementById("profile-name").textContent = decodeText(response.name) || "";
      document.getElementById("profile-nickname").textContent = decodeText(response.nickname) || "";
      document.getElementById("profile-email").textContent = response.email || "";
      document.getElementById("profile-phone").textContent = response.phone || "";
      document.getElementById("profile-birthdate").textContent = response.birthDate || "";
      document.getElementById("profile-gender").textContent = response.gender || "";

    } catch (error) {
      handleApiError(error);
    }
  }

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

  // 뉴스 표시 함수
  function displayNews(news) {
    document.getElementById("news-title").textContent = news.title;
    document.getElementById("published-date").textContent = `발행일: ${news.published_date}`;
    document.getElementById(
      "category-levels"
    ).textContent = `카테고리: ${news.category_level1}, ${news.category_level2}, ${news.category_level3}`;
    document.getElementById("news-content").innerHTML = news.content;
    document.getElementById("press").textContent = `출처: ${news.press}`;
    document.getElementById("reporter").textContent = `기자: ${news.reporter}`;
    document.getElementById("address").href = news.address;
    document.getElementById("address").textContent = "원문보기";
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
});
