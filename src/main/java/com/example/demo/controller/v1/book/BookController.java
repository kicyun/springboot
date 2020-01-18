package com.example.demo.controller.v1.book;

import com.example.demo.model.book.SearchHistoryResult;
import com.example.demo.model.book.SearchRankResult;
import com.example.demo.model.response.CommonResult;
import com.example.demo.model.response.ListResult;
import com.example.demo.service.BookService;
import com.example.demo.service.ResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"3.Book"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/book")
public class BookController {
    private final BookService bookService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "책 검색", notes = "책을 검색한다.")
    @GetMapping(value = "/search/{keyword}")
    public CommonResult search(@PathVariable String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uid = authentication.getName();
        bookService.search(uid, keyword);
        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "책 검색 기록", notes = "책 검색 기록을 반환한다.")
    @GetMapping(value = "/search/history")
    public ListResult<SearchHistoryResult> history() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uid = authentication.getName();
        return responseService.getListResult(bookService.searchHistory(uid));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "인기 키워드 목록", notes = "인기 키워드 목록을 반환한다.")
    @GetMapping(value = "/search/rank")
    public ListResult<SearchRankResult> rank() {
        return responseService.getListResult(bookService.searchRank());
    }
}
