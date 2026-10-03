import argparse
from pathlib import Path
import json, subprocess, concurrent.futures, time
parser = argparse.ArgumentParser(description="Audit the JARs recorded in a line alignment manifest.")
parser.add_argument("--manifest", type=Path, default=Path(__file__).with_name("manifest.json"))
parser.add_argument("--classpath", required=True, help="Compiled project classes, runtime dependencies and audit harness directory")
parser.add_argument("--java", default="java")
parser.add_argument("--output", type=Path, required=True)
parser.add_argument("--workers", type=int, default=4)
args = parser.parse_args()
out = args.output
out.mkdir(parents=True, exist_ok=True)
jars = json.loads(args.manifest.read_text())["jars"]
java = args.java
cp = args.classpath
fields=['attempted','succeeded','errors','numbered','matched','mismatched','suppressed','bytecode','no_numbers']
def audit(jar):
 totals=dict.fromkeys(fields,0); details=[]
 for start in range(0,jar['classes'],100):
  stem=jar['name']+'-'+str(start); result=out/(stem+'.tsv')
  with (out/(stem+'.log')).open('w') as log:
   process=subprocess.run([java,'-Xmx768m','-cp',cp,'JarAlignmentAudit',jar['path'],str(start),'100',str(result)],stdout=log,stderr=subprocess.STDOUT,timeout=180)
  if process.returncode or not result.exists(): raise RuntimeError(f'{stem}: exit {process.returncode}')
  lines=result.read_text().splitlines(); counts=list(map(int,lines[0].split('\t')))
  for k,v in zip(fields,counts): totals[k]+=v
  details.extend(lines[1:])
  print(f"{jar['name']} {min(start+100,jar['classes'])}/{jar['classes']}: matched {totals['matched']}, suppressed {totals['suppressed']}, mismatched {totals['mismatched']}, errors {totals['errors']}",flush=True)
  row=dict(jar,**totals,details=details)
  (out/(jar['name']+'.json')).write_text(json.dumps(row,indent=2))
 return row
with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as pool:
 futures=[pool.submit(audit,j) for j in jars]
 rows=[f.result() for f in futures]
(out/'summary.json').write_text(json.dumps(rows,indent=2))
print('DONE',flush=True)
