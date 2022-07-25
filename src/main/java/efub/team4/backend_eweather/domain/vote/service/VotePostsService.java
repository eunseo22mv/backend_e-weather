package efub.team4.backend_eweather.domain.vote.service;

import efub.team4.backend_eweather.domain.user.dto.SessionUser;
import efub.team4.backend_eweather.domain.user.entity.User;
import efub.team4.backend_eweather.domain.user.repository.UserRepository;
import efub.team4.backend_eweather.domain.vote.dto.VoteRequestDto;
import efub.team4.backend_eweather.domain.vote.dto.VoteResponseDto;
import efub.team4.backend_eweather.domain.vote.dto.VoteUpdateRequestDto;
import efub.team4.backend_eweather.domain.vote.entity.VotePosts;
import efub.team4.backend_eweather.domain.vote.entity.Votes;
import efub.team4.backend_eweather.domain.vote.repository.VotePostsRespsitory;
import efub.team4.backend_eweather.domain.vote.repository.VotesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VotePostsService {

    @Autowired
    private final VotePostsRespsitory votePostsRespsitory;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final VotesRepository votesRepository;

    public VoteResponseDto buildResponseDto(VotePosts entity){
        return new VoteResponseDto(entity);
    }

    @Transactional
    public VoteResponseDto savePost(SessionUser user, VoteRequestDto voteRequestDto) {
        // voteRequestDto -> entity

        UUID id = user.getId();

        User writer = userRepository.findById(id).get();

        VotePosts votePosts = VotePosts.builder()
                .user(writer)
                .building(voteRequestDto.getBuilding())
                .clothes(voteRequestDto.getClothes())
                .build();

        votePostsRespsitory.save(votePosts);

        Votes votes = Votes.builder()
                .user(writer)
                .votePosts(votePosts)
                .build();

        votesRepository.save(votes);

        return buildResponseDto(votePosts);

    }

    // 투표 게시글 수정
    public UUID update(UUID id, VoteUpdateRequestDto requestDto){
        VotePosts votePosts = votePostsRespsitory.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id " + id));

        votePosts.update(requestDto.getClothes());

        return id;
    }

    // 투표 게시글 전체 조회
    @Transactional(readOnly = true)
    public List<VoteResponseDto> findAllVotePostsList(){

        List<VotePosts> votePostsList = votePostsRespsitory.findAll();
        List<VoteResponseDto> voteResponseDtoList = new ArrayList<>();
        for (VotePosts votePost : votePostsList) {
            VoteResponseDto voteResponsetDto = new VoteResponseDto(votePost);
            voteResponseDtoList.add(voteResponsetDto);
        }
        return voteResponseDtoList;
    }

    // 투표 게시글 개별 조회 - UUID의 경우 findBy함수 사용 불가

    @Transactional
    public VoteResponseDto findById(UUID id){
        VotePosts entity = votePostsRespsitory.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id " + id));
        return new VoteResponseDto(entity);

    }

    // 투표 게시글 삭제
    public void deleteVotePost (UUID id){
        VotePosts votePosts = votePostsRespsitory.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("no votePost id = " + id));
        votePostsRespsitory.delete(votePosts);
    }

    // 좋아요
    public VoteResponseDto updateGood(UUID id, SessionUser sessionUser) {
        VotePosts votePosts = votePostsRespsitory.findById(id).orElseThrow(() -> new IllegalArgumentException("no votePost id = " + id));

        List<Votes> votesList = votesRepository.findAllByVotePosts(votePosts);

        Boolean isOkay = true;
        User user = userRepository.findByEmail(sessionUser.getEmail());

        for(Votes votes : votesList){
            System.out.println(votes.getUser().getId());
            User user1 = votes.getUser();
            if(user1.equals(user)){
                isOkay = false;
                System.out.println(votes.getVotePosts().getId() + "실패");
                return null;
            }
            else{
                continue;
            }
        }
        if(!isOkay){
            return null;
        } else {
            System.out.println("성공");
            votesRepository.save(new Votes(userRepository.findByEmail(sessionUser.getEmail()), votePosts));

            votePosts.updateGood();
            VotePosts response = votePostsRespsitory.save(votePosts);
            return buildResponseDto(response);
        }
    }

    // 싫어요
    public VoteResponseDto updateBad(UUID id, SessionUser sessionUser) {
        VotePosts votePosts = votePostsRespsitory.findById(id).orElseThrow(() -> new IllegalArgumentException("no votePost id = " + id));
        List<Votes> votesList = votesRepository.findAllByVotePosts(votePosts);

        Boolean isOkay = true;
        User user = userRepository.findByEmail(sessionUser.getEmail());

        for(Votes votes : votesList){
            System.out.println(votes.getUser().getId());
            User user1 = votes.getUser();
            if(user1.equals(user)){
                isOkay = false;
                System.out.println(votes.getVotePosts().getId() + "실패");
                return null;
            }
            else{
                continue;
            }
        }
        if(!isOkay){
            return null;
        } else {
            System.out.println("성공");
            votesRepository.save(new Votes(userRepository.findByEmail(sessionUser.getEmail()), votePosts));

            votePosts.updateBad();
            VotePosts response = votePostsRespsitory.save(votePosts);
            return buildResponseDto(response);
        }
    }
}