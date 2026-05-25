package com.example.production.service;

import com.example.production.entity.Career;
import com.example.production.entity.LearningResource;
import com.example.production.repositpry.CareerRepository;
import com.example.production.repositpry.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LearningResourceSeedService {

    private static final String ACTIVE = "ACTIVE";

    private final CareerRepository careerRepository;
    private final LearningResourceRepository learningResourceRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedLearningResources() {
        ResourceSeed[] seeds = {
                new ResourceSeed("Công nghệ thông tin", "Lộ trình nhập môn Công nghệ thông tin", "Tổng quan ngành IT, tư duy lập trình, cơ sở dữ liệu và các hướng nghề phổ biến.", "ROADMAP", "BEGINNER", "https://roadmap.sh/computer-science"),
                new ResourceSeed("Công nghệ thông tin", "CS50x Introduction to Computer Science", "Khóa học nền tảng về lập trình, thuật toán và tư duy giải quyết vấn đề.", "VIDEO", "BEGINNER", "https://cs50.harvard.edu/x/"),
                new ResourceSeed("Kỹ thuật phần mềm", "Software Engineering Roadmap", "Các kỹ năng cần học để phát triển phần mềm: Git, testing, architecture và deployment.", "ROADMAP", "INTERMEDIATE", "https://roadmap.sh/software-design-architecture"),
                new ResourceSeed("Kỹ thuật phần mềm", "Thực hành thiết kế REST API", "Bài tập xây dựng API theo chuẩn REST, validation, phân trang và xử lý lỗi.", "EXERCISE", "INTERMEDIATE", "https://www.baeldung.com/rest-with-spring-series"),
                new ResourceSeed("Khoa học máy tính", "Data Structures and Algorithms", "Tài liệu học cấu trúc dữ liệu, thuật toán và độ phức tạp tính toán.", "WEBSITE", "INTERMEDIATE", "https://www.geeksforgeeks.org/data-structures/"),
                new ResourceSeed("Khoa học máy tính", "MIT OpenCourseWare Algorithms", "Bài giảng thuật toán từ MIT cho người muốn học sâu về khoa học máy tính.", "VIDEO", "ADVANCED", "https://ocw.mit.edu/courses/6-006-introduction-to-algorithms-spring-2020/"),
                new ResourceSeed("An toàn thông tin", "OWASP Web Security Testing Guide", "Tài liệu kiểm thử bảo mật web, phù hợp để học về lỗ hổng phổ biến.", "PDF", "INTERMEDIATE", "https://owasp.org/www-project-web-security-testing-guide/"),
                new ResourceSeed("An toàn thông tin", "PortSwigger Web Security Academy", "Kho bài lab thực hành bảo mật web từ cơ bản đến nâng cao.", "EXERCISE", "BEGINNER", "https://portswigger.net/web-security"),
                new ResourceSeed("Marketing", "Digital Marketing Basics", "Tổng quan SEO, content, social media, ads và đo lường hiệu quả chiến dịch.", "WEBSITE", "BEGINNER", "https://learndigital.withgoogle.com/digitalgarage"),
                new ResourceSeed("Marketing", "Content Marketing Toolkit", "Bộ hướng dẫn lập kế hoạch nội dung, chân dung khách hàng và lịch đăng bài.", "PDF", "BEGINNER", "https://www.hubspot.com/resources"),
                new ResourceSeed("Kinh tế", "Khan Academy Economics", "Nền tảng kinh tế vi mô, vĩ mô, cung cầu, lạm phát và chính sách tiền tệ.", "VIDEO", "BEGINNER", "https://www.khanacademy.org/economics-finance-domain"),
                new ResourceSeed("Kinh tế", "World Bank Open Data", "Nguồn dữ liệu kinh tế thế giới để luyện phân tích và làm báo cáo.", "WEBSITE", "INTERMEDIATE", "https://data.worldbank.org/"),
                new ResourceSeed("Kế toán - Kiểm toán", "Accounting Coach", "Tài liệu học nguyên lý kế toán, báo cáo tài chính và bút toán cơ bản.", "WEBSITE", "BEGINNER", "https://www.accountingcoach.com/"),
                new ResourceSeed("Kế toán - Kiểm toán", "Audit Evidence Practice", "Bài tập nhận diện bằng chứng kiểm toán và đánh giá rủi ro sai sót.", "EXERCISE", "INTERMEDIATE", "https://www.accaglobal.com/gb/en/student/exam-support-resources.html"),
                new ResourceSeed("Tài chính - Ngân hàng", "Corporate Finance Institute", "Kiến thức tài chính doanh nghiệp, định giá, ngân hàng và mô hình tài chính.", "WEBSITE", "BEGINNER", "https://corporatefinanceinstitute.com/resources/"),
                new ResourceSeed("Tài chính - Ngân hàng", "Financial Modeling Roadmap", "Lộ trình học Excel tài chính, báo cáo, dự báo và phân tích đầu tư.", "ROADMAP", "INTERMEDIATE", "https://corporatefinanceinstitute.com/resources/financial-modeling/"),
                new ResourceSeed("Ngôn ngữ Anh", "BBC Learning English", "Bài học nghe, phát âm, từ vựng và ngữ pháp theo chủ đề.", "VIDEO", "BEGINNER", "https://www.bbc.co.uk/learningenglish"),
                new ResourceSeed("Ngôn ngữ Anh", "IELTS Writing Practice", "Bài tập luyện viết IELTS Task 1 và Task 2 kèm tiêu chí chấm điểm.", "EXERCISE", "INTERMEDIATE", "https://www.ielts.org/for-test-takers/sample-test-questions"),
                new ResourceSeed("Luật", "Thư viện Pháp luật", "Nguồn tra cứu văn bản pháp luật Việt Nam, nghị định, thông tư và án lệ.", "WEBSITE", "BEGINNER", "https://thuvienphapluat.vn/"),
                new ResourceSeed("Luật", "IRAC Legal Writing Guide", "Hướng dẫn phân tích tình huống pháp lý theo Issue, Rule, Application, Conclusion.", "PDF", "INTERMEDIATE", "https://www.law.cuny.edu/legal-writing/students/irac-crracc/"),
                new ResourceSeed("Y dược", "Merck Manual Medical Student Resources", "Tài liệu y khoa nền tảng về triệu chứng, bệnh học và hướng tiếp cận lâm sàng.", "WEBSITE", "INTERMEDIATE", "https://www.merckmanuals.com/professional"),
                new ResourceSeed("Y dược", "Anatomy Learning Roadmap", "Lộ trình học giải phẫu, sinh lý và ôn tập bằng flashcard cho sinh viên y dược.", "ROADMAP", "BEGINNER", "https://www.kenhub.com/"),
                new ResourceSeed("Thiết kế đồ họa", "Canva Design School", "Bài học về bố cục, màu sắc, typography và thiết kế truyền thông.", "VIDEO", "BEGINNER", "https://www.canva.com/designschool/"),
                new ResourceSeed("Thiết kế đồ họa", "Figma Learn Design", "Tài liệu học UI, wireframe, component, prototype và hệ thống thiết kế.", "WEBSITE", "BEGINNER", "https://www.figma.com/resources/learn-design/")
        };

        for (ResourceSeed seed : seeds) {
            if (learningResourceRepository.existsByTitleIgnoreCase(seed.title())) {
                continue;
            }

            Career career = careerRepository.findFirstByNameIgnoreCase(seed.careerName())
                    .orElseGet(() -> careerRepository.save(new Career(null, seed.careerName())));

            learningResourceRepository.save(LearningResource.builder()
                    .career(career)
                    .title(seed.title())
                    .description(seed.description())
                    .resourceType(seed.type())
                    .level(seed.level())
                    .url(seed.url())
                    .status(ACTIVE)
                    .build());
        }
    }

    private record ResourceSeed(
            String careerName,
            String title,
            String description,
            String type,
            String level,
            String url) {
    }
}
