"""
Import sample data for recommendation engine
"""

import predictionio
import argparse
import random

RATE_ACTIONS_DELIMITER = "::"
SEED = 3

def import_events(client, file):
  f = open(file, 'r')
  random.seed(SEED)
  count = 0
  print "Importing data..."
  for line in f:
    data = line.rstrip('\r\n').split(RATE_ACTIONS_DELIMITER)
    if len(data) < 16:
      continue
    client.create_event(
      event="twitter",
      entity_type="question",
      entity_id=data[5],
      properties={"text":data[2], "gender":data[6], "dizziness":data[7], "convulsions":data[8], "heart_palpitation":data[9], "shortness_of_breath":data[10], "headaches":data[11], "effect_decreased":data[12], "allergies_worse":data[13], "bad_interation":data[14], "nausea":data[15], "insomnia":data[16]}
    )
    count += 1
  f.close()
  print "%s events are imported." % count

if __name__ == '__main__':
  parser = argparse.ArgumentParser(
    description="Import sample data for recommendation engine")
  parser.add_argument('--access_key', default='invald_access_key')
  parser.add_argument('--url', default="http://localhost:7070")
  parser.add_argument('--file', default="./data/medwhatdat.txt")

  args = parser.parse_args()
  print args

  client = predictionio.EventClient(
    access_key=args.access_key,
    url=args.url,
    threads=5,
    qsize=500)
  import_events(client, args.file)
