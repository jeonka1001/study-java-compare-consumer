/**
 * 섀도잉 데이터 규격 (Consumer 측 정의).
 *
 * <p><b>이 패키지는 Proxy 서버의 {@code shadowdiff.proxy.contract}와 짝을 이룬다.</b>
 * 두 프로젝트는 서로 의존하지 않고 각자 동일한 규격을 정의한다.
 * 따라서 <b>단일 기준(Source of Truth)은 코드가 아니라 ShadowDiff README "3. 데이터 규격(Data Contract)" 문서</b>이다.
 *
 * <h2>변경 시 반드시 지킬 것</h2>
 * <ol>
 *     <li>README 3장을 먼저 수정한다.</li>
 *     <li>Producer(proxy)와 Consumer(이 패키지) 양쪽을 함께 반영한다.</li>
 *     <li>필드 추가는 안전하지만, <b>필드 삭제·타입 변경·이름 변경은 호환성을 깬다.</b>
 *         Consumer를 먼저 배포한 뒤 Producer를 배포한다.</li>
 * </ol>
 *
 * <p>양측 모두 {@code @JsonIgnoreProperties(ignoreUnknown = true)}를 사용하므로
 * 필드 추가만으로는 상대 프로젝트가 깨지지 않는다.
 */
package shadowdiff.compare.contract;
