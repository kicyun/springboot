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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Api(tags = {"2.Book"})
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/search/book")
public class BookController {
    private final BookService bookService;
    private final ResponseService responseService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "책 검색", notes = "책을 검색한다.")
    @GetMapping(value = "/{keyword}")
    public CommonResult search(
                @PathVariable(name = "keyword") String keyword,
                @RequestParam(name = "page", defaultValue = "1") Integer page) throws InterruptedException, ExecutionException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uid = authentication.getName();
        bookService.saveSearchHistory(uid, keyword);
        bookService.incrementSearchCount(keyword);
        CompletableFuture<String> searchFuture = bookService.search(keyword, page);
        CompletableFuture.allOf(searchFuture).join();
        return responseService.getSingleResult(searchFuture.get());
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "로그인 성공 후 access_token", required = true, dataType = "String", paramType = "header")
    })
    @ApiOperation(value = "책 검색 기록", notes = "책 검색 기록을 반환한다.")
    @GetMapping(value = "/history")
    public ListResult<SearchHistoryResult> history() throws InterruptedException, ExecutionException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String uid = authentication.getName();
        CompletableFuture<List<SearchHistoryResult>> searchHistoryFuture = bookService.getSearchHistory(uid);
        CompletableFuture.allOf(searchHistoryFuture).join();
        return responseService.getListResult(searchHistoryFuture.get());
    }

    @ApiOperation(value = "인기 키워드 목록", notes = "인기 키워드 목록을 반환한다.")
    @GetMapping(value = "/rank")
    public ListResult<SearchRankResult> rank() throws InterruptedException, ExecutionException {
        CompletableFuture<List<SearchRankResult>> searchRankFuture = bookService.getSearchRank();
        CompletableFuture.allOf(searchRankFuture).join();
        return responseService.getListResult(searchRankFuture.get());
    }
}
