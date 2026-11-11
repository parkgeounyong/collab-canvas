#!/usr/bin/env python3
"""
JaCoCo XML 리포트를 읽어 파일별 LINE·BRANCH 커버리지와 미커버 라인을 출력한다.

사용법:
    python3 .claude/scripts/coverage.py                 # 측정 대상 전체
    python3 .claude/scripts/coverage.py MemberService   # 이름에 해당 문자열이 들어간 파일만

종료 코드:
    0  모든 대상이 LINE·BRANCH 100%
    1  기준 미달 파일이 있음
    2  리포트가 없음 (./gradlew test 를 먼저 실행)

원칙: 커버리지 수치는 이 스크립트의 출력만 근거로 보고한다.
      AI 가 HTML 리포트를 직접 읽고 해석하지 않는다.
"""
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

REPORT = Path("build/reports/jacoco/test/jacocoTestReport.xml")


def counter(elem, kind):
    for c in elem.findall("counter"):
        if c.get("type") == kind:
            missed, covered = int(c.get("missed")), int(c.get("covered"))
            return covered, missed + covered
    return 0, 0


def pct(covered, total):
    return "-" if total == 0 else f"{covered / total * 100:.1f}%"


def compress(numbers):
    """[3,4,5,9] -> '3-5, 9'"""
    if not numbers:
        return ""
    ranges, start, prev = [], numbers[0], numbers[0]
    for n in numbers[1:]:
        if n == prev + 1:
            prev = n
            continue
        ranges.append(f"{start}-{prev}" if start != prev else f"{start}")
        start = prev = n
    ranges.append(f"{start}-{prev}" if start != prev else f"{start}")
    return ", ".join(ranges)


def main():
    if not REPORT.exists():
        print(f"[ERROR] {REPORT} 가 없습니다. ./gradlew test 를 먼저 실행하세요.")
        return 2

    filters = sys.argv[1:]
    root = ET.parse(REPORT).getroot()

    rows, failed = [], []
    for package in root.findall("package"):
        for source in package.findall("sourcefile"):
            path = f"{package.get('name')}/{source.get('name')}"
            if filters and not any(f in path for f in filters):
                continue
            lc, lt = counter(source, "LINE")
            bc, bt = counter(source, "BRANCH")
            missed_lines = sorted(
                int(l.get("nr")) for l in source.findall("line")
                if int(l.get("mi", 0)) > 0 and int(l.get("ci", 0)) == 0
            )
            partial_lines = sorted(
                int(l.get("nr")) for l in source.findall("line")
                if int(l.get("mb", 0)) > 0 and int(l.get("cb", 0)) > 0
            )
            ok = lc == lt and bc == bt
            rows.append((path, lc, lt, bc, bt, missed_lines, partial_lines, ok))
            if not ok:
                failed.append(path)

    if not rows:
        print("측정 대상 파일이 없습니다. (필터 또는 build.gradle.kts 의 coverageIncludes 확인)")
        return 0

    print("| 파일 | LINE | BRANCH | 미커버 라인 | 일부만 커버된 분기 라인 | 판정 |")
    print("|---|---|---|---|---|---|")
    for path, lc, lt, bc, bt, ml, pl, ok in rows:
        print(
            f"| {path} | {pct(lc, lt)} ({lc}/{lt}) | {pct(bc, bt)} ({bc}/{bt}) "
            f"| {compress(ml) or '-'} | {compress(pl) or '-'} | {'PASS' if ok else 'FAIL'} |"
        )

    print()
    print(f"합계: {len(rows)}개 파일 중 PASS {len(rows) - len(failed)}, FAIL {len(failed)}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
