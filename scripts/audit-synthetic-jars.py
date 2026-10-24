#!/usr/bin/env python3
"""Run SyntheticJarAudit in isolated JVMs, with a time limit for each jar.

First build the test classpath:
  mvn -DskipTests test-compile dependency:build-classpath -Dmdep.outputFile=target/audit-classpath
Then pass a manifest containing one jar path per line (relative to --repository or absolute).
Candidate identifiers require manual triage; legitimate user identifiers can contain '$'.
"""

import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
import json
import os
from pathlib import Path
import subprocess


def scan(item, options, classpath):
    ordinal, jar = item
    output = options.output / str(ordinal)
    output.mkdir(parents=True, exist_ok=True)
    result = {"jar": str(jar), "output": str(output)}
    with (output / "scan.log").open("w") as log:
        try:
            command = ["java", "-Xmx768m", "-cp", classpath, "jd.core.test.SyntheticJarAudit", str(jar), str(output)]
            if options.targets is not None:
                targets = options.class_targets.get(str(jar.resolve()), [])
                target_file = output / "classes.txt"
                target_file.write_text("\n".join(targets) + "\n")
                command.append(str(target_file))
            process = subprocess.run(
                command,
                stdout=log, stderr=subprocess.STDOUT, timeout=options.timeout, check=False,
            )
            result["exitCode"] = process.returncode
        except subprocess.TimeoutExpired:
            result["timeout"] = True
    log_lines = (output / "scan.log").read_text().splitlines()
    result["attemptedClasses"] = sum(line.startswith("CLASS\t") for line in log_lines)
    totals = [line for line in log_lines if line.startswith("TOTAL\t")]
    if totals:
        result.update({key: int(value) for key, value in
                       (field.split("=", 1) for field in totals[-1].split("\t")[1:])})
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("manifest", type=Path)
    parser.add_argument("--repository", type=Path, default=Path.home() / ".m2/repository")
    parser.add_argument("--output", type=Path, default=Path("target/synthetic-audit"))
    parser.add_argument("--classpath", type=Path, default=Path("target/audit-classpath"))
    parser.add_argument("--targets", type=Path, help="TSV of jar path and internal class name for focused reruns")
    parser.add_argument("--worker-classes", type=Path, default=Path("target/test-classes"))
    parser.add_argument("--classes", type=Path, default=Path("target/classes"))
    parser.add_argument("--workers", type=int, default=3)
    parser.add_argument("--timeout", type=int, default=100)
    options = parser.parse_args()
    if options.workers < 1 or options.timeout < 1:
        parser.error("--workers and --timeout must be positive")
    jars = [options.repository / line.strip() for line in options.manifest.read_text().splitlines()
            if line.strip() and not line.lstrip().startswith("#")]
    if not jars or any(not jar.is_file() for jar in jars):
        parser.error("manifest must contain existing jar files")
    options.class_targets = {}
    if options.targets is not None:
        for line in options.targets.read_text().splitlines():
            jar, name = line.split("\t")
            key = str((options.repository / jar).resolve())
            options.class_targets.setdefault(key, []).append(name)
        if any(str(jar.resolve()) not in options.class_targets for jar in jars):
            parser.error("--targets must name at least one class in each manifest jar")
    if options.output.exists():
        parser.error("--output must be a new directory to keep previous findings intact")
    options.output.mkdir(parents=True)
    classpath = os.pathsep.join([str(options.worker_classes), str(options.classes), options.classpath.read_text().strip()])
    results = []
    with ThreadPoolExecutor(max_workers=options.workers) as pool:
        pending = [pool.submit(scan, item, options, classpath) for item in enumerate(jars)]
        for future in as_completed(pending):
            result = future.result()
            results.append(result)
            print(json.dumps(result), flush=True)
            (options.output / "summary.json").write_text(json.dumps(results, indent=2) + "\n")


if __name__ == "__main__":
    main()
