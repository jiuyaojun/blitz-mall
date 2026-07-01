package com.blitz.ai.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blitz.ai.api.dto.KbDocumentVO;
import com.blitz.ai.application.service.KnowledgeBaseService;
import com.blitz.ai.domain.model.KbDocument;
import com.blitz.ai.domain.port.KbDocumentRepository;
import com.blitz.common.result.PageResult;
import com.blitz.common.result.Result;
import com.blitz.security.annotation.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai/kb")
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!minimal")
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final KbDocumentRepository documentRepository;

    /** 上传文档（文本内容） */
    @PostMapping("/document")
    @RequireRole("ADMIN")
    public Result<KbDocumentVO> upload(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "TXT") String fileType) {
        KbDocument doc = knowledgeBaseService.ingestDocument(title, content, fileType);
        return Result.success(toVO(doc));
    }

    /** 删除文档 */
    @DeleteMapping("/document/{id}")
    @RequireRole("ADMIN")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeBaseService.deleteDocument(id);
        return Result.success();
    }

    /** 文档列表 */
    @GetMapping("/document")
    @RequireRole("ADMIN")
    public Result<PageResult<KbDocumentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<KbDocument> result = documentRepository.findAllPage(new Page<>(page, size));
        List<KbDocumentVO> vos = result.getRecords().stream().map(this::toVO).toList();
        return Result.success(new PageResult<>(vos, result.getTotal(), page, size));
    }

    private KbDocumentVO toVO(KbDocument doc) {
        return new KbDocumentVO(doc.getId(), doc.getTitle(), doc.getFileType(),
            doc.getStatus(), doc.getChunkCount(), doc.getCreateTime());
    }
}
