import React, { useState } from 'react';

const TravelDrawerTab = () => {
  const [searchBoard, setSearchBoard] = useState('');
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchItemType, setSearchItemType] = useState('');

  const [bookmarkedPosts] = useState([
    { id: 1, title: '아름다운 해동용궁사 여행 후기', board: '리뷰' },
    { id: 2, title: '불국사 방문 전 알아야 할 팁', board: '사찰정보' },
    { id: 3, title: '조계사 템플스테이 체험기', board: '리뷰' },
  ]);

  const [likedItems] = useState([
    { id: 1, type: '게시글', title: '송광사, 고즈넉한 아름다움', board: '사찰정보' },
    { id: 2, type: '댓글', content: '저도 여기 너무 좋았어요!', board: '리뷰' },
    { id: 3, type: '게시글', title: '백양사 가을 풍경', board: '리뷰' },
  ]);

  const handleSearch = (e) => {
    e.preventDefault();
    alert(`검색 조건: 게시판 - ${searchBoard}, 키워드 - "${searchKeyword}" (API 연동 필요)`);
    console.log('검색 조건:', { searchBoard, searchKeyword });
  };

  const handleLikedSearch = (e) => {
    e.preventDefault();
    alert(`좋아요 검색 조건: 게시판 - ${searchBoard}, 타입 - ${searchItemType}, 키워드 - "${searchKeyword}" (API 연동 필요)`);
    console.log('좋아요 검색 조건:', { searchBoard, searchItemType, searchKeyword });
  };

  return (
    <div className="tab-content-section travel-drawer-tab">
      <h3 className="tab-content-title">여행 서랍 🎒</h3>

      <h4>내가 찜한 게시글 보기</h4>
      <form onSubmit={handleSearch} className="search-form">
        <select value={searchBoard} onChange={(e) => setSearchBoard(e.target.value)}>
          <option value="">게시판 선택</option>
          <option value="사찰정보">사찰정보</option>
          <option value="리뷰">리뷰</option>
        </select>
        <input
          type="text"
          placeholder="검색 키워드 입력"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <button type="submit" className="search-button">검색</button>
      </form>
      <div className="post-list">
        {bookmarkedPosts.length > 0 ? (
          bookmarkedPosts.map(post => (
            <div key={post.id} className="post-item">
              <span className="post-title">[{post.board}] {post.title}</span>
              <button className="action-button">자세히 보기</button>
            </div>
          ))
        ) : (
          <p>찜한 게시글이 없어요. 😢</p>
        )}
      </div>

      <h4 style={{marginTop: '40px'}}>내가 좋아요한 게시글/댓글 보기</h4>
      <form onSubmit={handleLikedSearch} className="search-form">
        <select value={searchBoard} onChange={(e) => setSearchBoard(e.target.value)}>
          <option value="">게시판 선택</option>
          <option value="사찰정보">사찰정보</option>
          <option value="리뷰">리뷰</option>
        </select>
        <select value={searchItemType} onChange={(e) => setSearchItemType(e.target.value)}>
          <option value="">전체</option>
          <option value="게시글">게시글</option>
          <option value="댓글">댓글</option>
        </select>
        <input
          type="text"
          placeholder="검색 키워드 입력"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <button type="submit" className="search-button">검색</button>
      </form>
      <div className="post-list">
        {likedItems.length > 0 ? (
          likedItems.map(item => (
            <div key={item.id} className="post-item">
              <span className="post-title">[{item.type}] [{item.board}] {item.title || item.content}</span>
              <button className="action-button">자세히 보기</button>
            </div>
          ))
        ) : (
          <p>좋아요한 게시글이나 댓글이 없어요. 😢</p>
        )}
      </div>
    </div>
  );
};

export default TravelDrawerTab;